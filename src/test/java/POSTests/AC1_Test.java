package POSTests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import POSPD.*;

public class AC1_Test {
    private Store store;
    private Item item1, item2, item3;
    private UPC upc1, upc2, upc3;
    private Price price1, price2, price3;
    private TaxCategory tC1, tC2, tC3;

    @BeforeEach
    public void setUp() {
        store = new Store("Store Store", "0001");
        item1 = new Item("001", "Wal*Mart");
        item2 = new Item("002", "Target ");
        item3 = new Item("003", "Home Depot");
        upc1 = new UPC("0000000000010110", item1);
        upc2 = new UPC("0000000000010111", item2);
        upc3 = new UPC("0000000000011000", item3);
        price1 = new Price("48.4", "9/16/18");
        price2 = new Price("136", "10/14/19");
        price3 = new Price("144", "4/4/98");
        tC1 = new TaxCategory("Store");
        tC2 = new TaxCategory("Shop");
        tC3 = new TaxCategory("Tienda");

        item1.addPrice(price1);
        item1.setTaxCategory(tC1);
        item1.addUPC(upc1);
        price1.setItem(item1);

        item2.addPrice(price2);
        item2.setTaxCategory(tC2);
        item2.addUPC(upc2);
        price2.setItem(item2);

        item3.addPrice(price3);
        item3.setTaxCategory(tC3);
        item3.addUPC(upc3);
        price3.setItem(item3);

        store.addItem(item1);
        store.addItem(item2);
        store.addItem(item3);

    }

    @Test
    public void test() {
        assertNotNull(store);
        assertEquals(3, store.getItems().size(), "Store should have 3 items");
    }
}
