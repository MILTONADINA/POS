package POSTests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import POSPD.*;
import POSDM.DataManagement;

public class AC5_Test {

    private Store myStore;

    @BeforeEach
    public void setUp() {
        myStore = new Store();
        DataManagement.loadStore(myStore);
    }

    @Test
    public void test() {
        assertNotNull(myStore);
        assertTrue(myStore.getItems().size() > 0, "Store should have loaded items from CSV");
        assertNotNull(myStore.getName(), "Store should have a name");
    }
}
