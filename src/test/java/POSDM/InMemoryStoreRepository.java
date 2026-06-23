package POSDM;

import POSPD.Store;

/**
 * Test double that keeps the store in memory, so service-layer tests need no disk I/O and never
 * touch the real data file. Records whether {@link #save(Store)} was called.
 */
public class InMemoryStoreRepository implements StoreRepository {

    private Store store;
    private int saveCount;

    public InMemoryStoreRepository(Store store) {
        this.store = store;
    }

    @Override
    public Store load() {
        return store;
    }

    @Override
    public void save(Store store) {
        this.store = store;
        saveCount++;
    }

    public int saveCount() {
        return saveCount;
    }
}
