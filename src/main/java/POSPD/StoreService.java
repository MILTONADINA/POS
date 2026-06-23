package POSPD;

import POSDM.StoreRepository;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Application service orchestrating operations between the UI and the {@link Store} domain graph.
 *
 * <p>It is the single entry point for state-changing operations and the only collaborator that
 * talks to persistence. Persistence is injected as a {@link StoreRepository}, so the service can be
 * unit tested against an in-memory fake and is not bound to any particular storage technology.
 */
public class StoreService {

    private final Store store;
    private final StoreRepository repository;

    /**
     * Creates a service that loads its store from the given repository.
     *
     * @param repository the persistence backing store
     */
    public StoreService(StoreRepository repository) {
        this.repository = repository;
        this.store = repository.load();
    }

    /**
     * Creates a service over an already-loaded store (primarily for tests).
     *
     * @param store the in-memory store
     * @param repository the persistence backing store
     */
    public StoreService(Store store, StoreRepository repository) {
        this.store = store;
        this.repository = repository;
    }

    public Store getStore() {
        return this.store;
    }

    /** Persists the current state of the store domain graph. */
    public void saveStoreState() {
        repository.save(this.store);
    }

    /**
     * Authenticates a cashier and, on success, opens a session and persists it.
     *
     * @param cashierId the cashier number
     * @param password the plaintext password (blank is always rejected)
     * @param register the register the session opens on
     * @return the new {@link Session} if authentication succeeds, otherwise empty
     */
    public Optional<Session> login(String cashierId, String password, Register register) {
        if (password == null || password.isEmpty()) {
            return Optional.empty();
        }
        Cashier cashier = store.findCashierForNumber(cashierId);
        if (cashier != null && cashier.isAuthorized(password)) {
            // Now that we hold the verified plaintext, transparently re-hash a credential whose
            // work
            // factor is below the current standard, so stored hashes harden over time on login.
            if (PasswordHasher.needsRehash(cashier.getPassword())) {
                cashier.setPassword(password);
            }
            Session session = new Session(cashier, register, store);
            store.addSession(session);
            saveStoreState();
            return Optional.of(session);
        }
        return Optional.empty();
    }

    /**
     * Ends the active session and persists the change.
     *
     * @param session the session to end
     */
    public void endSession(Session session) {
        session.setEndDateTime(LocalDateTime.now());
        saveStoreState();
    }

    /**
     * Adds an item to the store and persists the change.
     *
     * @param item the item to add
     */
    public void addItem(Item item) {
        store.addItem(item);
        saveStoreState();
    }

    /**
     * Adds a cashier to the store and persists the change.
     *
     * @param cashier the cashier to add
     */
    public void addCashier(Cashier cashier) {
        store.addCashier(cashier);
        saveStoreState();
    }

    /**
     * Adds a register to the store and persists the change.
     *
     * @param register the register to add
     */
    public void addRegister(Register register) {
        store.addRegister(register);
        saveStoreState();
    }

    /**
     * Completes a sale within a session and persists the change.
     *
     * @param session the session the sale belongs to
     * @param sale the completed sale
     */
    public void completeSale(Session session, Sale sale) {
        session.addSale(sale);
        saveStoreState();
    }
}
