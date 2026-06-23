package POSDM;

/**
 * Thrown when the store cannot be read from or written to its backing store. Unlike the original
 * code, which swallowed persistence failures to {@code System.out} and returned a silently-empty
 * store, this surfaces the failure so callers can react (e.g. warn the cashier that a sale was not
 * saved).
 */
public class StorePersistenceException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public StorePersistenceException(String message, Throwable cause) {
        super(message, cause);
    }

    public StorePersistenceException(String message) {
        super(message);
    }
}
