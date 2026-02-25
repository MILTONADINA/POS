package POSTests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import POSPD.*;

public class AC3_Test {

    private Store store;
    private Register register1, register2;

    @BeforeEach
    public void setUp() {
        store = new Store("Store Store", "0001");
        register1 = new Register("01");
        register2 = new Register("02");

        store.addRegister(register1);
        store.addRegister(register2);

    }

    @Test
    public void test() {
        assertNotNull(store);
        assertEquals(2, store.getRegisters().size(), "Store should have 2 registers");
    }
}
