package POSPD;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Verifies the PBKDF2 password hasher. */
class PasswordHasherTest {

    @Test
    @DisplayName("a hashed password verifies and a wrong one does not")
    void hashAndVerify() {
        String hash = PasswordHasher.hash("correct horse");
        assertTrue(PasswordHasher.verify("correct horse", hash));
        assertFalse(PasswordHasher.verify("battery staple", hash));
    }

    @Test
    @DisplayName("the same password hashes to different values (random salt)")
    void saltMakesHashesUnique() {
        assertNotEquals(PasswordHasher.hash("pw12345"), PasswordHasher.hash("pw12345"));
    }

    @Test
    @DisplayName("verify rejects null/empty candidates and malformed stored hashes")
    void rejectsBadInput() {
        String hash = PasswordHasher.hash("pw12345");
        assertFalse(PasswordHasher.verify(null, hash));
        assertFalse(PasswordHasher.verify("", hash));
        assertFalse(PasswordHasher.verify("pw12345", "not-a-valid-hash"));
        assertFalse(PasswordHasher.verify("pw12345", null));
    }

    @Test
    @DisplayName("hashing null/empty is rejected")
    void hashRejectsEmpty() {
        assertThrows(IllegalArgumentException.class, () -> PasswordHasher.hash(""));
        assertThrows(IllegalArgumentException.class, () -> PasswordHasher.hash(null));
    }

    @Test
    @DisplayName("legacy/unknown hash formats are flagged for rehash; current ones are not")
    void needsRehash() {
        assertTrue(PasswordHasher.needsRehash("deadbeef")); // legacy bare SHA-256-style
        assertTrue(PasswordHasher.needsRehash(null));
        assertFalse(PasswordHasher.needsRehash(PasswordHasher.hash("pw12345")));
    }

    @Test
    @DisplayName("verify returns false (never throws) for malformed PBKDF2 parameters")
    void rejectsMalformedParameters() {
        // Correct prefix and field count, but out-of-range parameters that would otherwise throw
        // inside PBKDF2 (empty salt, zero iterations, negative iterations).
        assertFalse(PasswordHasher.verify("pw12345", "pbkdf2_sha256$600000$$ZGlnZXN0"));
        assertFalse(PasswordHasher.verify("pw12345", "pbkdf2_sha256$0$c2FsdA$ZGlnZXN0"));
        assertFalse(PasswordHasher.verify("pw12345", "pbkdf2_sha256$-5$c2FsdA$ZGlnZXN0"));
    }
}
