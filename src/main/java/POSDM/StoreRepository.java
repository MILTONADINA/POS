package POSDM;

import POSPD.Store;

/**
 * Abstraction over store persistence. Decoupling the service layer from a concrete CSV/disk
 * implementation lets the store be loaded and saved against any backing store (file, in-memory fake
 * for tests, a future database) and makes the service unit-testable in isolation.
 */
public interface StoreRepository {

    /**
     * Loads the persisted store.
     *
     * @return the loaded {@link Store}; an empty store if no data source exists yet
     * @throws StorePersistenceException if the data source exists but cannot be read
     */
    Store load();

    /**
     * Persists the current state of the store.
     *
     * @param store the store to save
     * @throws StorePersistenceException if the store cannot be written
     */
    void save(Store store);
}
