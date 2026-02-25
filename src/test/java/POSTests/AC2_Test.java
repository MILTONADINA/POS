package POSTests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import POSPD.*;

public class AC2_Test {

    private Store store;
    private Cashier cash1, cash2, cash3;
    private Person per1, per2, per3;

    @BeforeEach
    public void setUp() {
        store = new Store("Store Store", "0001");
        per1 = new Person("Bobby Bobson", "123456", "123 Bob St.", "Bobville", "BA", "123456", "123-456-7890", null);
        cash1 = new Cashier("01", per1, "Bob");
        per1.setCashier(cash1);
        per2 = new Person("Poppy Popson", "123456", "123 Pop St.", "Popville", "PA", "123456", "123-456-7890", null);
        cash2 = new Cashier("02", per2, "Pop");
        per2.setCashier(cash2);
        per3 = new Person("Daddy Dad-san", "123456", "123 Dad St.", "Dadville", "DA", "123456", "123-456-7890", null);
        cash3 = new Cashier("03", per3, "Dad");
        per3.setCashier(cash3);

        store.addCashier(cash1);
        store.addCashier(cash2);
        store.addCashier(cash3);

    }

    @Test
    public void test() {
        assertNotNull(store);
        assertEquals(3, store.getCashiers().size(), "Store should have 3 cashiers");
    }
}
