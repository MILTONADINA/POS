package POSDM;

import POSPD.Cash;
import POSPD.Cashier;
import POSPD.Check;
import POSPD.Credit;
import POSPD.Item;
import POSPD.Payment;
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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * CSV-backed {@link StoreRepository}.
 *
 * <p>The serialization format is a flat, line-oriented CSV where the first field of every line is a
 * record tag. Unlike the original implementation, the format is fully symmetric (so a save followed
 * by a load reproduces the object graph), uses dedicated {@code UPC}/{@code Price}/{@code
 * PromoPrice} records (so items with zero, one, or many UPCs and prices all round-trip), persists
 * session timestamps, encodes the tax-free flag canonically, and stores only a masked card number.
 *
 * <p>Fields are encoded with RFC-4180 quoting, so values containing commas, quotes, or newlines
 * round-trip faithfully. Parsing is defensive: each line is validated for its expected field count
 * and a malformed line (or a record referencing an unknown item or tax category) is logged and
 * skipped rather than aborting the whole load. Reads prefer the configured data file and fall back
 * to the bundled classpath seed; writes always target the configured data file (never the source
 * tree), creating parent directories as needed.
 */
public class CsvStoreRepository implements StoreRepository {

    private static final Logger LOG = Logger.getLogger(CsvStoreRepository.class.getName());
    private static final String SEED_RESOURCE = "/StoreData_v2024FA.csv";
    private static final String DEFAULT_FILE_PROPERTY = "pos.data.file";
    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    // Record tags, shared by reader and writer so the two halves cannot drift apart.
    private static final String STORE = "Store";
    private static final String TAX_CATEGORY = "TaxCategory";
    private static final String CASHIER = "Cashier";
    private static final String ITEM = "Item";
    private static final String UPC_REC = "UPC";
    private static final String PRICE = "Price";
    private static final String PROMO_PRICE = "PromoPrice";
    private static final String REGISTER = "Register";
    private static final String SESSION = "Session";
    private static final String SALE = "Sale";
    private static final String SALE_LINE_ITEM = "SaleLineItem";
    private static final String PAYMENT = "Payment";
    private static final String CASH = "Cash";
    private static final String CREDIT = "Credit";
    private static final String CHECK = "Check";

    private final Path dataFile;

    /**
     * Creates a repository backed by a specific file.
     *
     * @param dataFile the file to read from (when present) and write to
     */
    public CsvStoreRepository(Path dataFile) {
        this.dataFile = dataFile;
    }

    /**
     * Creates the default repository: the path given by the {@code pos.data.file} system property,
     * or {@code ${user.home}/.pos/StoreData_v2024FA.csv} otherwise. The first load falls back to
     * the bundled seed when this file does not yet exist.
     *
     * @return the default repository
     */
    public static CsvStoreRepository defaultRepository() {
        String configured = System.getProperty(DEFAULT_FILE_PROPERTY);
        Path path =
                (configured != null && !configured.isBlank())
                        ? Path.of(configured)
                        : Path.of(System.getProperty("user.home"), ".pos", "StoreData_v2024FA.csv");
        return new CsvStoreRepository(path);
    }

    @Override
    public Store load() {
        Store store = new Store();
        try (BufferedReader reader = openForRead()) {
            if (reader == null) {
                LOG.info("No store data file or seed resource found; starting with an empty store");
                return store;
            }
            parse(reader, store);
        } catch (IOException e) {
            throw new StorePersistenceException("Failed to read store data from " + dataFile, e);
        }
        return store;
    }

    @Override
    public void save(Store store) {
        try {
            Path parent = dataFile.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            try (PrintWriter writer =
                    new PrintWriter(Files.newBufferedWriter(dataFile, StandardCharsets.UTF_8))) {
                write(store, writer);
            }
        } catch (IOException e) {
            throw new StorePersistenceException("Failed to write store data to " + dataFile, e);
        }
    }

    private BufferedReader openForRead() throws IOException {
        if (Files.exists(dataFile)) {
            return Files.newBufferedReader(dataFile, StandardCharsets.UTF_8);
        }
        InputStream seed = CsvStoreRepository.class.getResourceAsStream(SEED_RESOURCE);
        if (seed != null) {
            return new BufferedReader(new InputStreamReader(seed, StandardCharsets.UTF_8));
        }
        return null;
    }

    // ----- Parsing -------------------------------------------------------------------------------

    private void parse(BufferedReader reader, Store store) throws IOException {
        Session currentSession = null;
        Sale currentSale = null;
        String line;
        int lineNo = 0;
        while ((line = reader.readLine()) != null) {
            lineNo++;
            if (line.isBlank()) {
                continue;
            }
            String[] f = splitCsv(line);
            try {
                switch (f[0]) {
                    case STORE:
                        require(f, 2, lineNo);
                        store.setName(f[1]);
                        break;
                    case TAX_CATEGORY:
                        parseTaxCategory(f, lineNo, store);
                        break;
                    case CASHIER:
                        require(f, 10, lineNo);
                        store.addCashier(cashierFrom(f));
                        break;
                    case ITEM:
                        require(f, 4, lineNo);
                        TaxCategory category = requireTaxCategory(store, f[3], lineNo);
                        Item item = new Item(f[1], f[2]);
                        item.setTaxCategory(category);
                        store.addItem(item);
                        break;
                    case UPC_REC:
                        require(f, 3, lineNo);
                        new UPC(f[2], requireItem(store, f[1], lineNo));
                        break;
                    case PRICE:
                        require(f, 4, lineNo);
                        requireItem(store, f[1], lineNo).addPrice(new Price(f[2], f[3]));
                        break;
                    case PROMO_PRICE:
                        require(f, 5, lineNo);
                        requireItem(store, f[1], lineNo).addPrice(new PromoPrice(f[2], f[3], f[4]));
                        break;
                    case REGISTER:
                        require(f, 2, lineNo);
                        store.addRegister(new Register(f[1]));
                        break;
                    case SESSION:
                        currentSession = sessionFrom(f, lineNo, store);
                        currentSale = null;
                        break;
                    case SALE:
                        require(f, 2, lineNo);
                        currentSale = new Sale(f[1]);
                        requireSession(currentSession, lineNo).addSale(currentSale);
                        break;
                    case SALE_LINE_ITEM:
                        require(f, 3, lineNo);
                        requireSale(currentSale, lineNo)
                                .addSaleLineItem(new SaleLineItem(f[1], f[2], store));
                        break;
                    case PAYMENT:
                        requireSale(currentSale, lineNo).addPayment(paymentFrom(f, lineNo));
                        break;
                    default:
                        LOG.warning(
                                "Skipping unknown record type '" + f[0] + "' at line " + lineNo);
                }
            } catch (RuntimeException e) {
                LOG.log(Level.WARNING, "Skipping malformed line " + lineNo + ": " + line, e);
            }
        }
    }

    private void parseTaxCategory(String[] f, int lineNo, Store store) {
        require(f, 4, lineNo);
        TaxCategory category = store.getTaxCategory(f[1]);
        if (category == null) {
            category = new TaxCategory(f[1]);
            store.addTaxCategory(category);
        }
        category.addTaxRate(new TaxRate(f[3], f[2]));
    }

    private Cashier cashierFrom(String[] f) {
        // f[9] is an already-hashed credential; the constructor stores it verbatim (no re-hash).
        return new Cashier(f[1], f[2], f[3], f[4], f[5], f[6], f[7], f[8], f[9]);
    }

    private Session sessionFrom(String[] f, int lineNo, Store store) {
        require(f, 3, lineNo);
        Session session = new Session(f[1], f[2], store);
        if (f.length >= 4 && !f[3].isBlank()) {
            session.setStartDateTime(LocalDateTime.parse(f[3], TIMESTAMP));
        }
        if (f.length >= 5 && !f[4].isBlank()) {
            session.setEndDateTime(LocalDateTime.parse(f[4], TIMESTAMP));
        }
        store.addSession(session);
        return session;
    }

    private Payment paymentFrom(String[] f, int lineNo) {
        require(f, 2, lineNo);
        switch (f[1]) {
            case CASH:
                require(f, 4, lineNo);
                return new Cash(f[2], f[3]);
            case CREDIT:
                require(f, 7, lineNo);
                Credit credit = new Credit(f[4], f[5], f[6]);
                credit.setAmount(f[2]);
                credit.setAmtTendered(f[3]);
                return credit;
            case CHECK:
                require(f, 7, lineNo);
                Check check = new Check(f[2], f[3], f[5], f[6]);
                check.setRoutingNumber(f[4]);
                return check;
            default:
                throw new IllegalArgumentException("Unknown payment type: " + f[1]);
        }
    }

    // ----- Writing -------------------------------------------------------------------------------

    private void write(Store store, PrintWriter writer) {
        writer.println(row(STORE, store.getName()));

        for (TaxCategory tc : store.getTaxCategories().values()) {
            for (TaxRate tr : tc.getTaxRates()) {
                writer.println(
                        row(
                                TAX_CATEGORY,
                                tc.getCategory(),
                                tr.getTaxRate().toString(),
                                POSPD.DateUtils.format(tr.getEffectiveDate())));
            }
        }

        for (Cashier c : store.getCashiers().values()) {
            writer.println(
                    row(
                            CASHIER,
                            c.getNumber(),
                            c.getPerson().getName(),
                            c.getPerson().getSSN(),
                            c.getPerson().getAddress(),
                            c.getPerson().getCity(),
                            c.getPerson().getState(),
                            c.getPerson().getZip(),
                            c.getPerson().getPhone(),
                            c.getPassword()));
        }

        for (Item item : store.getItems().values()) {
            writer.println(
                    row(
                            ITEM,
                            item.getNumber(),
                            item.getDescription(),
                            item.getTaxCategory().getCategory()));
            for (UPC upc : item.getUpcs().values()) {
                writer.println(row(UPC_REC, item.getNumber(), upc.getUPC()));
            }
            for (Price p : item.getPrices()) {
                if (p instanceof PromoPrice) {
                    PromoPrice promo = (PromoPrice) p;
                    writer.println(
                            row(
                                    PROMO_PRICE,
                                    item.getNumber(),
                                    promo.getPrice().toString(),
                                    POSPD.DateUtils.format(promo.getEffectiveDate()),
                                    POSPD.DateUtils.format(promo.getEndDate())));
                } else {
                    writer.println(
                            row(
                                    PRICE,
                                    item.getNumber(),
                                    p.getPrice().toString(),
                                    POSPD.DateUtils.format(p.getEffectiveDate())));
                }
            }
        }

        for (Register r : store.getRegisters().values()) {
            writer.println(row(REGISTER, r.getNumber()));
        }

        for (Session s : store.getSessions()) {
            String start =
                    s.getStartDateTime() != null ? s.getStartDateTime().format(TIMESTAMP) : "";
            String end = s.getEndDateTime() != null ? s.getEndDateTime().format(TIMESTAMP) : "";
            writer.println(
                    row(
                            SESSION,
                            s.getCashier().getNumber(),
                            s.getRegister().getNumber(),
                            start,
                            end));
            for (Sale sale : s.getSales()) {
                writer.println(row(SALE, String.valueOf(sale.getTaxFree())));
                for (SaleLineItem sli : sale.getSaleLineItems()) {
                    writer.println(
                            row(
                                    SALE_LINE_ITEM,
                                    sli.getItem().getNumber(),
                                    String.valueOf(sli.getQuantity())));
                }
                for (Payment p : sale.getPayments()) {
                    writePayment(p, writer);
                }
            }
        }
    }

    private void writePayment(Payment p, PrintWriter writer) {
        if (p instanceof Cash) {
            writer.println(
                    row(PAYMENT, CASH, p.getAmount().toString(), p.getAmtTendered().toString()));
        } else if (p instanceof Credit) {
            Credit c = (Credit) p;
            writer.println(
                    row(
                            PAYMENT,
                            CREDIT,
                            c.getAmount().toString(),
                            c.getAmtTendered().toString(),
                            c.getCardType(),
                            c.getMaskedAcctNumber(),
                            POSPD.DateUtils.format(c.getExpireDate())));
        } else if (p instanceof Check) {
            Check c = (Check) p;
            writer.println(
                    row(
                            PAYMENT,
                            CHECK,
                            c.getAmount().toString(),
                            c.getAmtTendered().toString(),
                            c.getRoutingNumber(),
                            c.getAccountNumber(),
                            c.getCheckNumber()));
        }
    }

    // ----- CSV field encoding (RFC 4180) ---------------------------------------------------------

    /** Joins fields into one CSV record, quoting/escaping any field that needs it. */
    private static String row(String... fields) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fields.length; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(encode(fields[i]));
        }
        return sb.toString();
    }

    /** RFC 4180 field encoding: wrap in quotes (doubling embedded quotes) when needed. */
    private static String encode(String field) {
        String value = field == null ? "" : field;
        if (value.indexOf(',') >= 0
                || value.indexOf('"') >= 0
                || value.indexOf('\n') >= 0
                || value.indexOf('\r') >= 0) {
            return '"' + value.replace("\"", "\"\"") + '"';
        }
        return value;
    }

    /** RFC 4180 tokenizer: splits one record into fields, honoring quotes and escaped quotes. */
    private static String[] splitCsv(String line) {
        java.util.List<String> fields = new java.util.ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (inQuotes) {
                if (ch == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        current.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    current.append(ch);
                }
            } else if (ch == '"') {
                inQuotes = true;
            } else if (ch == ',') {
                fields.add(current.toString());
                current.setLength(0);
            } else {
                current.append(ch);
            }
        }
        fields.add(current.toString());
        return fields.toArray(new String[0]);
    }

    // ----- Validation helpers --------------------------------------------------------------------

    private static void require(String[] fields, int min, int lineNo) {
        if (fields.length < min) {
            throw new IllegalArgumentException(
                    "Expected at least "
                            + min
                            + " fields but found "
                            + fields.length
                            + " at line "
                            + lineNo);
        }
    }

    private static Item requireItem(Store store, String itemNumber, int lineNo) {
        Item item = store.findItemForNumber(itemNumber);
        if (item == null) {
            throw new IllegalArgumentException(
                    "Reference to unknown item '" + itemNumber + "' at line " + lineNo);
        }
        return item;
    }

    private static TaxCategory requireTaxCategory(Store store, String categoryName, int lineNo) {
        TaxCategory category = store.getTaxCategory(categoryName);
        if (category == null) {
            throw new IllegalArgumentException(
                    "Reference to unknown tax category '" + categoryName + "' at line " + lineNo);
        }
        return category;
    }

    private static Session requireSession(Session session, int lineNo) {
        if (session == null) {
            throw new IllegalArgumentException(
                    "Record at line " + lineNo + " has no enclosing session");
        }
        return session;
    }

    private static Sale requireSale(Sale sale, int lineNo) {
        if (sale == null) {
            throw new IllegalArgumentException(
                    "Record at line " + lineNo + " has no enclosing sale");
        }
        return sale;
    }
}
