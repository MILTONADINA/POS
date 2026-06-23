package POSPD;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Salted, iterated password hashing built on PBKDF2-HMAC-SHA256.
 *
 * <p>Hashes are stored in a self-describing, Django-style PHC string: {@code
 * pbkdf2_sha256$<iterations>$<base64-salt>$<base64-hash>}. Encoding the algorithm, iteration count
 * and salt alongside the digest lets the verifier stay forward-compatible if the work factor is
 * raised later, and lets {@link #needsRehash(String)} flag stored hashes that should be upgraded on
 * the user's next successful login.
 *
 * <p>Verification uses a constant-time comparison ({@link MessageDigest#isEqual}) to avoid leaking
 * information through timing. This class is stateless and thread-safe.
 */
public final class PasswordHasher {

    /** Identifier written into the encoded hash so the format is self-describing. */
    static final String ALGORITHM_ID = "pbkdf2_sha256";

    private static final String SECRET_KEY_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int DEFAULT_ITERATIONS = 600_000;
    private static final int SALT_LENGTH_BYTES = 16;
    private static final int KEY_LENGTH_BITS = 256;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private PasswordHasher() {
        // Utility class; not instantiable.
    }

    /**
     * Hashes a plaintext password with a freshly generated random salt.
     *
     * @param password the plaintext password; must be non-null and non-empty
     * @return the encoded PHC-style hash string
     * @throws IllegalArgumentException if {@code password} is null or empty
     */
    public static String hash(String password) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password must not be null or empty");
        }
        byte[] salt = new byte[SALT_LENGTH_BYTES];
        SECURE_RANDOM.nextBytes(salt);
        byte[] digest = pbkdf2(password.toCharArray(), salt, DEFAULT_ITERATIONS, KEY_LENGTH_BITS);
        Base64.Encoder encoder = Base64.getEncoder().withoutPadding();
        return ALGORITHM_ID
                + "$"
                + DEFAULT_ITERATIONS
                + "$"
                + encoder.encodeToString(salt)
                + "$"
                + encoder.encodeToString(digest);
    }

    /**
     * Verifies a plaintext password against a previously {@link #hash(String) encoded} hash.
     *
     * @param password the plaintext candidate (a null/empty candidate never matches)
     * @param storedHash the encoded hash produced by {@link #hash(String)}
     * @return {@code true} only if the password matches the stored hash
     */
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(
            value = "UNSAFE_HASH_EQUALS",
            justification =
                    "equals() here compares only the public algorithm-identifier prefix"
                            + " (\"pbkdf2_sha256\"), not the secret digest. The secret is compared in"
                            + " constant time via MessageDigest.isEqual, so there is no timing oracle.")
    public static boolean verify(String password, String storedHash) {
        if (password == null || password.isEmpty() || storedHash == null) {
            return false;
        }
        String[] parts = storedHash.split("\\$");
        if (parts.length != 4 || !ALGORITHM_ID.equals(parts[0])) {
            return false;
        }
        final int iterations;
        final byte[] salt;
        final byte[] expected;
        try {
            iterations = Integer.parseInt(parts[1]);
            Base64.Decoder decoder = Base64.getDecoder();
            salt = decoder.decode(parts[2]);
            expected = decoder.decode(parts[3]);
        } catch (IllegalArgumentException e) {
            return false; // malformed stored hash
        }
        byte[] actual = pbkdf2(password.toCharArray(), salt, iterations, expected.length * 8);
        return MessageDigest.isEqual(actual, expected);
    }

    /**
     * Reports whether a stored hash uses an outdated algorithm or work factor and should be
     * re-hashed the next time the password is known (e.g. on successful login).
     *
     * @param storedHash the encoded hash to inspect
     * @return {@code true} if the value is not in the current format or is below the current
     *     iteration count
     */
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(
            value = "UNSAFE_HASH_EQUALS",
            justification =
                    "Compares only the public algorithm-identifier prefix; no secret is compared.")
    public static boolean needsRehash(String storedHash) {
        if (storedHash == null) {
            return true;
        }
        String[] parts = storedHash.split("\\$");
        if (parts.length != 4 || !ALGORITHM_ID.equals(parts[0])) {
            return true;
        }
        try {
            return Integer.parseInt(parts[1]) < DEFAULT_ITERATIONS;
        } catch (NumberFormatException e) {
            return true;
        }
    }

    private static byte[] pbkdf2(char[] password, byte[] salt, int iterations, int keyLengthBits) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyLengthBits);
            try {
                SecretKeyFactory factory = SecretKeyFactory.getInstance(SECRET_KEY_ALGORITHM);
                return factory.generateSecret(spec).getEncoded();
            } finally {
                spec.clearPassword();
            }
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("PBKDF2 algorithm unavailable in this JVM", e);
        }
    }
}
