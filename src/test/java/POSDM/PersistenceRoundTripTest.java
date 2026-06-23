package POSDM;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import POSPD.Cash;
import POSPD.Cashier;
import POSPD.Check;
import POSPD.Credit;
import POSPD.Item;
import POSPD.Payment;
import POSPD.Person;
import POSPD.Price;
import POSPD.PromoPrice;
import POSPD.Register;
import POSPD.Sale;
import POSPD.SaleLineItem;
import POSPD.Session;
import POSPD.Store;
import POSPD.TaxCategory;
import POSPD.TaxRate;
import POSPD.UPC;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Verifies that the CSV persistence layer is lossless and resilient. */
class PersistenceRoundTripTest {

    private static final LocalDateTime START = LocalDateTime.of(2024, 9, 15, 8, 0, 0);
    private static final LocalDateTime END = LocalDateTime.of(2024, 9, 15, 16, 0, 0);
    private static final LocalDateTime SALE1_DATE = LocalDateTime.of(2024, 9, 15, 8, 5, 0);
    private static final LocalDateTime SALE2_DATE = LocalDateTime.of(2024, 9, 15, 9, 10, 0);

    private Store buildStore() {
        Store store = new Store("David's Quick Mart", "");
        TaxCategory food = new TaxCategory("Food");
        food.addTaxRate(new TaxRate(LocalDate.of(2000, 1, 1), new BigDecimal("0.07")));
        store.addTaxCategory(food);

        store.addCashier(
                new Cashier(
                        "1",
                        new Person(
                                "David",
                                "000-00-0001",
                                "1 St",
                                "Edmond",
                                "OK",
                                "73034",
                                "555.1111",
                                null),
                        "demo1234"));

        Register register = new Register("1");
        store.addRegister(register);

        Item sandwich = new Item("1001", "Turkey Sandwich");
        sandwich.setTaxCategory(food);
        new UPC("11111111111", sandwich);
        new UPC("99999999999", sandwich); // second UPC: exercises multi-UPC round-trip
        sandwich.addPrice(new Price("2.59", "1/1/2000"));
        sandwich.addPrice(new PromoPrice("2.29", "1/1/2000", "1/1/2100"));
        store.addItem(sandwich);

        Cashier cashier = store.findCashierForNumber("1");
        Session session = new Session(cashier, register);
        session.setStartDateTime(START);
        session.setEndDateTime(END);
        store.addSession(session);

        Sale taxFree = new Sale("true");
        taxFree.setDate(SALE1_DATE);
        new SaleLineItem(taxFree, sandwich, 1);
        taxFree.addPayment(new Cash("2.29", "3.00"));
        session.addSale(taxFree);

        Sale taxable = new Sale("false");
        taxable.setDate(SALE2_DATE);
        new SaleLineItem(taxable, sandwich, 2);
        Credit credit = new Credit("VISA", "4111111111111111", "1/1/2030");
        credit.setAmount("4.90");
        credit.setAmtTendered("4.90");
        taxable.addPayment(credit);
        Check check = new Check("2.50", "2.50", "987654321", "1001");
        check.setRoutingNumber("123456789");
        taxable.addPayment(check);
        session.addSale(taxable);

        return store;
    }

    @Test
    @DisplayName("a full store round-trips losslessly through save and load")
    void roundTrip(@TempDir Path dir) {
        Path file = dir.resolve("store.csv");
        CsvStoreRepository repo = new CsvStoreRepository(file);
        repo.save(buildStore());
        Store loaded = new CsvStoreRepository(file).load();

        assertEquals("David's Quick Mart", loaded.getName());
        assertEquals(1, loaded.getCashiers().size());
        assertEquals(1, loaded.getItems().size());

        // Multi-UPC item round-trips (the original format lost extra UPCs).
        Item sandwich = loaded.findItemForNumber("1001");
        assertEquals(2, sandwich.getUpcs().size());
        assertEquals(2, sandwich.getPrices().size());

        // Tax rate and promo price survive.
        assertEquals(
                0,
                new BigDecimal("0.07")
                        .compareTo(food(loaded).getTaxRateForDate(LocalDate.of(2024, 1, 1))));
        assertEquals(
                0,
                new BigDecimal("2.29")
                        .compareTo(sandwich.getPriceForDate(LocalDate.of(2024, 1, 1)).getPrice()));

        // Credentials survive (the hash round-trips and still authenticates).
        assertTrue(loaded.findCashierForNumber("1").isAuthorized("demo1234"));

        // Session timestamps survive (the original discarded them).
        Session session = loaded.getSessions().get(0);
        assertEquals(START, session.getStartDateTime());
        assertEquals(END, session.getEndDateTime());

        // Sales, tax-free flag, and payment subtypes survive.
        assertEquals(2, session.getSales().size());
        assertTrue(session.getSales().get(0).getTaxFree(), "first sale should stay tax-free");
        assertFalse(session.getSales().get(1).getTaxFree());
        assertTrue(session.getSales().get(0).getPayments().get(0) instanceof Cash);
        Payment p = session.getSales().get(1).getPayments().get(0);
        assertTrue(p instanceof Credit);
        assertEquals("************1111", ((Credit) p).getMaskedAcctNumber());

        // Sale dates round-trip (no longer revert to load-time "now").
        assertEquals(SALE1_DATE, session.getSales().get(0).getDate());
        assertEquals(SALE2_DATE, session.getSales().get(1).getDate());

        // Check bank account is masked at rest.
        Payment checkPayment = session.getSales().get(1).getPayments().get(1);
        assertTrue(checkPayment instanceof Check);
        assertEquals("*****4321", ((Check) checkPayment).getAccountNumber());
    }

    @Test
    @DisplayName("the full card number is never written to disk")
    void panIsNeverPersisted(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("store.csv");
        new CsvStoreRepository(file).save(buildStore());
        String contents = Files.readString(file);
        assertFalse(contents.contains("4111111111111111"), "raw PAN must not be persisted");
        assertTrue(contents.contains("************1111"), "masked PAN should be persisted");
        assertFalse(contents.contains("987654321"), "raw check account must not be persisted");
        assertTrue(contents.contains("*****4321"), "masked check account should be persisted");
    }

    @Test
    @DisplayName("fields containing commas and quotes round-trip via RFC-4180 quoting")
    void delimitersInFieldsRoundTrip(@TempDir Path dir) {
        Path file = dir.resolve("store.csv");
        Store store = new Store("Acme, Inc. \"Mart\"", "");
        TaxCategory food = new TaxCategory("Food");
        food.addTaxRate(new TaxRate(LocalDate.of(2000, 1, 1), new BigDecimal("0.07")));
        store.addTaxCategory(food);
        store.addCashier(
                new Cashier(
                        "1",
                        new Person(
                                "Sue",
                                "000-00-0009",
                                "123 Main St, Apt 4",
                                "Town",
                                "ST",
                                "00000",
                                "555.0000",
                                null),
                        "demo1234"));
        Item item = new Item("9", "Soda, 12oz");
        item.setTaxCategory(food);
        item.addPrice(new Price("1.00", "1/1/2000"));
        store.addItem(item);

        new CsvStoreRepository(file).save(store);
        Store loaded = new CsvStoreRepository(file).load();

        assertEquals("Acme, Inc. \"Mart\"", loaded.getName());
        assertEquals("Soda, 12oz", loaded.findItemForNumber("9").getDescription());
        assertEquals(
                "123 Main St, Apt 4", loaded.findCashierForNumber("1").getPerson().getAddress());
        // The credential column survived the comma-containing address that precedes it.
        assertTrue(loaded.findCashierForNumber("1").isAuthorized("demo1234"));
        // Tax category still resolves, so tax calculation does not throw.
        assertEquals(
                0,
                new BigDecimal("0.07")
                        .compareTo(
                                loaded.findItemForNumber("9")
                                        .getTaxRate(LocalDate.of(2024, 1, 1))));
    }

    @Test
    @DisplayName("a formula-like field is neutralized on disk but round-trips unchanged")
    void formulaInjectionNeutralized(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("store.csv");
        Store store = new Store("Mart", "");
        TaxCategory food = new TaxCategory("Food");
        food.addTaxRate(new TaxRate(LocalDate.of(2000, 1, 1), new BigDecimal("0.07")));
        store.addTaxCategory(food);
        Item item = new Item("1", "=1+1"); // a spreadsheet formula trigger as free text
        item.setTaxCategory(food);
        item.addPrice(new Price("1.00", "1/1/2000"));
        store.addItem(item);

        new CsvStoreRepository(file).save(store);
        String contents = Files.readString(file);
        assertTrue(
                contents.contains("'=1+1"),
                "formula trigger should be neutralized with a sentinel");

        Store loaded = new CsvStoreRepository(file).load();
        assertEquals("=1+1", loaded.findItemForNumber("1").getDescription());
    }

    @Test
    @DisplayName("values beginning with an apostrophe or formula trigger round-trip exactly")
    void leadingApostropheRoundTrips(@TempDir Path dir) {
        Path file = dir.resolve("store.csv");
        Store store = new Store("'=evil", ""); // apostrophe immediately followed by a trigger
        TaxCategory food = new TaxCategory("Food");
        food.addTaxRate(new TaxRate(LocalDate.of(2000, 1, 1), new BigDecimal("0.07")));
        store.addTaxCategory(food);
        Item item = new Item("1", "'=1+1");
        item.setTaxCategory(food);
        item.addPrice(new Price("1.00", "1/1/2000"));
        store.addItem(item);

        new CsvStoreRepository(file).save(store);
        Store loaded = new CsvStoreRepository(file).load();
        assertEquals("'=evil", loaded.getName());
        assertEquals("'=1+1", loaded.findItemForNumber("1").getDescription());
    }

    @Test
    @DisplayName(
            "a present but unparseable data file is rejected, not loaded as a silent empty store")
    void corruptFileRejected(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("store.csv");
        Files.writeString(file, "  garbage binary\nnot a known record type\n");
        assertThrows(StorePersistenceException.class, () -> new CsvStoreRepository(file).load());
    }

    @Test
    @DisplayName(
            "an item referencing an unknown tax category is skipped, not loaded with a null category")
    void unknownTaxCategorySkipped(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("store.csv");
        Files.writeString(
                file,
                String.join(
                                "\n",
                                "Store,Mart",
                                "TaxCategory,Food,0.07,1/1/24",
                                "Item,1,Good,Food",
                                "Item,2,Bad,Snacks") // 'Snacks' is never defined
                        + "\n");
        Store loaded = new CsvStoreRepository(file).load();
        assertEquals(1, loaded.getItems().size());
        assertNotNull(loaded.findItemForNumber("1"));
        assertNull(loaded.findItemForNumber("2"));
    }

    @Test
    @DisplayName("a sale line referencing an unknown item is skipped, and totals stay computable")
    void unknownItemLineSkipped(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("store.csv");
        Files.writeString(
                file,
                String.join(
                                "\n",
                                "Store,Mart",
                                "TaxCategory,Food,0.07,1/1/24",
                                "Item,1,Bread,Food",
                                "Price,1,2.00,1/1/24",
                                "Cashier,1,Amy,000-00-0001,1 St,Town,ST,00000,555,x",
                                "Register,1",
                                "Session,1,1,2024-01-01T08:00:00,2024-01-01T16:00:00",
                                "Sale,false,2024-01-01T09:00:00",
                                "SaleLineItem,1,2",
                                "SaleLineItem,999,1") // item 999 is undefined -> must be skipped
                        + "\n");
        Store loaded = new CsvStoreRepository(file).load();
        Sale sale = loaded.getSessions().get(0).getSales().get(0);
        assertEquals(1, sale.getSaleLineItems().size(), "the unknown-item line should be skipped");
        // Totals must not NPE on a null line item: 2.00 x2 = 4.00 + 7% tax = 4.28.
        assertEquals(0, new BigDecimal("4.28").compareTo(sale.calcTotal()));
    }

    @Test
    @DisplayName("a SALE line with a bad date is skipped without orphaning its following lines")
    void orphanSaleLinesNotAttached(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("store.csv");
        Files.writeString(
                file,
                String.join(
                                "\n",
                                "Store,Mart",
                                "TaxCategory,Food,0.07,1/1/24",
                                "Item,1,Bread,Food",
                                "Price,1,2.00,1/1/24",
                                "Cashier,1,Amy,000-00-0001,1 St,Town,ST,00000,555,x",
                                "Register,1",
                                "Session,1,1,2024-01-01T08:00:00,2024-01-01T16:00:00",
                                "Sale,false,NOT-A-DATE", // bad timestamp -> sale line skipped
                                "SaleLineItem,1,3", // must NOT attach to the skipped sale
                                "Payment,Cash,3.00,3.00",
                                "Sale,false,2024-01-01T09:00:00", // the valid sale
                                "SaleLineItem,1,1")
                        + "\n");
        Store loaded = new CsvStoreRepository(file).load();
        assertEquals(1, loaded.getSessions().size());
        // Only the valid sale (with its single line) survives; the orphan + its lines are dropped.
        assertEquals(1, loaded.getSessions().get(0).getSales().size());
        assertEquals(1, loaded.getSessions().get(0).getSales().get(0).getSaleLineItems().size());
    }

    @Test
    @DisplayName("a tax-category line with a non-numeric rate leaves no phantom category")
    void malformedTaxRateLeavesNoPhantomCategory(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("store.csv");
        Files.writeString(
                file,
                String.join(
                                "\n",
                                "Store,Mart",
                                "TaxCategory,Food,0.07,1/1/24",
                                "TaxCategory,Snacks,NOTANUMBER,1/1/24", // bad rate -> whole line
                                // skipped
                                "Item,1,Chips,Snacks", // references the absent Snacks -> skipped
                                "Item,2,Bread,Food")
                        + "\n");
        Store loaded = new CsvStoreRepository(file).load();
        assertNull(loaded.getTaxCategories().get("Snacks"), "no phantom empty category");
        assertNull(loaded.findItemForNumber("1"), "item referencing the bad category is skipped");
        assertNotNull(loaded.findItemForNumber("2"));
        assertEquals(1, loaded.getItems().size());
    }

    @Test
    @DisplayName("the bundled seed loads from the classpath when no data file exists")
    void seedLoadsFromClasspath(@TempDir Path dir) {
        // Point at a non-existent file so the repository falls back to the bundled seed resource.
        Store seed = new CsvStoreRepository(dir.resolve("does-not-exist.csv")).load();
        assertEquals(4, seed.getItems().size());
        assertEquals(2, seed.getCashiers().size());
        assertEquals(2, seed.getRegisters().size());
        assertTrue(seed.findCashierForNumber("1").isAuthorized("demo1234"));
        // First sale of the first session is tax-free in the seed.
        assertTrue(seed.getSessions().get(0).getSales().get(0).getTaxFree());
        // Promo price for item 1001 applies inside its window and expires afterward.
        Item sandwich = seed.findItemForNumber("1001");
        assertEquals(
                0,
                new BigDecimal("2.29")
                        .compareTo(sandwich.getPriceForDate(LocalDate.of(2024, 10, 1)).getPrice()));
        assertEquals(
                0,
                new BigDecimal("2.59")
                        .compareTo(sandwich.getPriceForDate(LocalDate.of(2025, 1, 1)).getPrice()));
    }

    @Test
    @DisplayName("a malformed line is skipped and the rest of the file still loads")
    void malformedLineIsSkipped(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("store.csv");
        Files.writeString(
                file,
                String.join(
                                "\n",
                                "Store,Resilient Mart",
                                "TaxCategory,Food", // malformed: missing rate and date
                                "Register,1",
                                "garbage line with no known tag",
                                "Register,2")
                        + "\n");
        Store loaded = new CsvStoreRepository(file).load();
        assertEquals("Resilient Mart", loaded.getName());
        assertEquals(2, loaded.getRegisters().size());
    }

    private TaxCategory food(Store store) {
        return store.getTaxCategories().get("Food");
    }
}
