package POSPD;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Verifies cashier credential handling and the plaintext-vs-hash distinction. */
class AuthenticationTest {

    @Test
    @DisplayName("a cashier authenticates with its password and rejects a wrong one")
    void authenticates() {
        Cashier cashier = new Cashier("1", new Person(), "s3cret-pw");
        assertTrue(cashier.isAuthorized("s3cret-pw"));
        assertFalse(cashier.isAuthorized("wrong"));
    }

    @Test
    @DisplayName("the stored credential is a salted hash, not the plaintext")
    void storesHashNotPlaintext() {
        Cashier cashier = new Cashier("1", new Person(), "s3cret-pw");
        assertNotEquals("s3cret-pw", cashier.getPassword());
        assertTrue(cashier.getPassword().startsWith("pbkdf2_sha256$"));
    }

    @Test
    @DisplayName("the same password produces different hashes (per-user salt)")
    void saltedHashesDiffer() {
        Cashier a = new Cashier("1", new Person(), "same-pw");
        Cashier b = new Cashier("2", new Person(), "same-pw");
        assertNotEquals(a.getPassword(), b.getPassword());
    }

    @Test
    @DisplayName("setPasswordHash stores an already-hashed value verbatim (no double-hash on load)")
    void setPasswordHashDoesNotReHash() {
        String hash = PasswordHasher.hash("loaded-pw");
        Cashier cashier = new Cashier();
        cashier.setPasswordHash(hash);
        assertTrue(cashier.isAuthorized("loaded-pw"));
    }

    @Test
    @DisplayName("the load-path constructor stores the credential without re-hashing it")
    void loadConstructorStoresVerbatim() {
        String hash = PasswordHasher.hash("loaded-pw");
        Cashier cashier =
                new Cashier("1", "Dave", "000-00-0001", "addr", "city", "ST", "00000", "555", hash);
        assertTrue(cashier.isAuthorized("loaded-pw"));
    }

    @Test
    @DisplayName("setting a blank password is rejected")
    void blankPasswordRejected() {
        Cashier cashier = new Cashier();
        assertThrows(IllegalArgumentException.class, () -> cashier.setPassword(""));
    }
}
