package POSPD;

import POSDM.DataManagement;
import java.util.Optional;

/**
 * Service orchestrating operations between the POSUI and the Store domain
 * models.
 * Decouples the UI's direct mutation of the domain elements and provides a
 * central entry point
 * for data persistence and transactional updates.
 */
public class StoreService {

    private final Store store;

    public StoreService(Store store) {
        this.store = store;
    }

    public Store getStore() {
        return this.store;
    }

    /**
     * Persists the current state of the store domain graph to disk.
     */
    public void saveStoreState() {
        DataManagement.saveStore(this.store);
    }

    /**
     * Authenticates a cashier and starts a physical session.
     */
    public Optional<Session> login(String cashierId, String password, Register register) {
        Cashier cashier = store.findCashierForNumber(cashierId);
        if (cashier != null && cashier.isAuthorized(password)) {
            Session session = new Session(cashier, register, store);
            store.addSession(session);
            saveStoreState();
            return Optional.of(session);
        }
        return Optional.empty();
    }

    /**
     * Ends the active session.
     */
    public void endSession(Session session) {
        session.setEndDateTime(java.time.LocalDateTime.now());
        saveStoreState();
    }

    /**
     * Adds an item to the store and persists the change.
     */
    public void addItem(Item item) {
        store.addItem(item);
        saveStoreState();
    }

    /**
     * Adds a cashier to the store and persists the change.
     */
    public void addCashier(Cashier cashier) {
        store.addCashier(cashier);
        saveStoreState();
    }

    /**
     * Adds a register to the store and persists the change.
     */
    public void addRegister(Register register) {
        store.addRegister(register);
        saveStoreState();
    }

    /**
     * Completes a sale within a session.
     */
    public void completeSale(Session session, Sale sale) {
        session.addSale(sale);
        saveStoreState();
    }
}
