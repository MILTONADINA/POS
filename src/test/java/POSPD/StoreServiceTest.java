package POSPD;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import POSDM.InMemoryStoreRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Verifies the service-layer login flow against an in-memory repository (no disk I/O). */
class StoreServiceTest {

    private StoreService service;
    private Register register;
    private InMemoryStoreRepository repository;

    @BeforeEach
    void setUp() {
        Store store = new Store("Test Store", "0001");
        store.addCashier(new Cashier("1", new Person(), "demo1234"));
        register = new Register("1");
        store.addRegister(register);
        repository = new InMemoryStoreRepository(store);
        service = new StoreService(store, repository);
    }

    @Test
    @DisplayName("valid credentials open and persist a session")
    void loginSucceeds() {
        Optional<Session> session = service.login("1", "demo1234", register);
        assertTrue(session.isPresent());
        assertEquals(1, service.getStore().getSessions().size());
        assertTrue(repository.saveCount() >= 1);
    }

    @Test
    @DisplayName("a wrong password is rejected and opens no session")
    void wrongPassword() {
        assertFalse(service.login("1", "wrong", register).isPresent());
        assertEquals(0, service.getStore().getSessions().size());
    }

    @Test
    @DisplayName("an unknown cashier is rejected")
    void unknownCashier() {
        assertFalse(service.login("999", "demo1234", register).isPresent());
    }

    @Test
    @DisplayName("a blank password is rejected without touching the store")
    void blankPassword() {
        assertFalse(service.login("1", "", register).isPresent());
        assertEquals(0, service.getStore().getSessions().size());
    }
}
