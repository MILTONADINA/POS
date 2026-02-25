package POSTests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;

import POSPD.*;

public class AC4_Test {

    private Store store;
    private Session session;
    private Cashier cashier;
    private Register register;
    private Sale sale;
    private Item item1, item2;
    private Price price1, price2, price4, price3;
    private TaxCategory tC1, tC2;
    private TaxRate tR1, tR2;
    private SaleLineItem sli1, sli2;

    @BeforeEach
    public void setUp() {
        store = new Store("Convenience Store", "0001");
        cashier = new Cashier("01", "Bobby Bobson", "123-456-7890", "123 Bob St.", "Bobville", "BA", "123456",
                "123-456-7890", "Bob");
        register = new Register("01");
        session = new Session(cashier, register, store);
        sale = new Sale("false");
        item1 = new Item("Coke", "00001");
        item2 = new Item("Skittles", "00002");
        price1 = new Price("0.5", "3/23/20");
        price2 = new Price("1.5", "4/25/98");
        price3 = new Price("0.25", "5/16/20");
        price4 = new Price("0.5", "6/23/01");
        tC1 = new TaxCategory("Soda");
        tC2 = new TaxCategory("Candy");
        tR1 = new TaxRate(LocalDate.of(1920, 5, 16), BigDecimal.valueOf(0.05));
        tR2 = new TaxRate(LocalDate.of(1998, 4, 25), BigDecimal.valueOf(0.02));

        tC1.addTaxRate(tR1);
        tC2.addTaxRate(tR2);

        item1.addPrice(price1);
        item1.addPrice(price2);
        item1.setTaxCategory(tC1);

        item2.addPrice(price3);
        item2.addPrice(price4);
        item2.setTaxCategory(tC2);

        store.addCashier(cashier);
        store.addRegister(register);
        store.addItem(item1);
        store.addItem(item2);

        session.addSale(sale);

        sli1 = new SaleLineItem(sale, item1, 1);
        sli2 = new SaleLineItem(sale, item2, 4);

    }

    @Test
    public void test() {
        assertNotNull(store);
        assertEquals(2, store.getItems().size(), "Store should have 2 items");
        assertEquals(1, session.getSales().size(), "Session should have 1 sale");
    }
}
