package POSPD;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Verifies UPC lookup stays correct when a UPC code is re-keyed (the edit-panel re-key path). */
class StoreLookupTest {

    @Test
    @DisplayName("re-keying a UPC code updates store lookup in memory (no reload needed)")
    void upcRekeyUpdatesLookup() {
        Store store = new Store("Mart", "");
        Item item = new Item("1", "Thing");
        store.addItem(item);
        UPC upc = new UPC("111", item); // self-registers with the item
        store.addUPC(upc);
        assertSame(item, store.findItemForUPC("111"));

        // Mirror the UpcEditPanel edit path: remove under the old code, change it, then re-add.
        item.removeUPC(upc);
        store.removeUPC(upc);
        upc.setUPC("222");
        item.addUPC(upc);
        store.addUPC(upc);

        assertNull(store.findItemForUPC("111"), "old code must no longer resolve");
        assertSame(item, store.findItemForUPC("222"), "new code must resolve to the item");
    }
}
