package POSPD;

import java.util.ArrayList;
import java.util.List;

/**
 * A cashier employed by the store, with login credentials and a history of work sessions.
 *
 * <p>Credentials are stored as a salted PBKDF2 hash (see {@link PasswordHasher}). The class
 * deliberately distinguishes {@link #setPassword(String)} (accepts a new <em>plaintext</em>
 * password and hashes it) from {@link #setPasswordHash(String)} (accepts an <em>already-hashed</em>
 * value, used by the persistence layer) so that loading a stored credential never re-hashes it.
 */
public class Cashier {

    /** Employee number identifying the cashier. */
    private String number;

    /** The person record (name, address, SSN, ...) for this cashier. */
    private Person person;

    /** Work sessions the cashier has worked. */
    private List<Session> sessions;

    /** Salted PBKDF2 hash of the cashier's password (never the plaintext). */
    private String password;

    /** Creates an empty cashier with no credential set. */
    public Cashier() {
        sessions = new ArrayList<>();
        person = new Person();
    }

    /**
     * Creates a cashier from its persisted fields. The supplied {@code passwordHash} is stored
     * verbatim (it is already hashed); it is <em>not</em> re-hashed.
     */
    public Cashier(
            String number,
            String name,
            String sSN,
            String address,
            String city,
            String state,
            String zip,
            String phone,
            String passwordHash) {
        this();
        this.number = number;
        this.person = new Person(name, sSN, address, city, state, zip, phone, this);
        this.password = passwordHash;
    }

    /**
     * Creates a cashier with a known {@link Person} and a new <em>plaintext</em> password, which is
     * hashed before storage.
     */
    public Cashier(String number, Person person, String plaintextPassword) {
        this();
        this.number = number;
        this.person = person;
        setPassword(plaintextPassword);
    }

    public String getNumber() {
        return this.number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public Person getPerson() {
        return this.person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    /** Returns the stored salted password hash (never the plaintext). */
    public String getPassword() {
        return this.password;
    }

    /**
     * Sets a new password from plaintext, hashing it with a fresh salt.
     *
     * @param plaintext the new plaintext password; must be non-null and non-empty
     * @throws IllegalArgumentException if {@code plaintext} is null or empty
     */
    public void setPassword(String plaintext) {
        this.password = PasswordHasher.hash(plaintext);
    }

    /**
     * Stores an already-hashed credential verbatim, without re-hashing. Used by the persistence
     * layer when reconstructing a cashier from disk.
     *
     * @param passwordHash an encoded hash previously produced by {@link
     *     PasswordHasher#hash(String)}
     */
    public void setPasswordHash(String passwordHash) {
        this.password = passwordHash;
    }

    /**
     * Verifies a candidate plaintext password against this cashier's stored hash.
     *
     * @param password the candidate plaintext
     * @return {@code true} only if the password matches
     */
    public boolean isAuthorized(String password) {
        return PasswordHasher.verify(password, this.password);
    }

    /**
     * Adds a work session to this cashier.
     *
     * @param session session to add
     */
    public void addSession(Session session) {
        sessions.add(session);
    }

    /**
     * Removes a work session from this cashier.
     *
     * @param session session to remove
     */
    public void removeSession(Session session) {
        sessions.remove(session);
    }

    /** Returns whether this cashier has any recorded sessions. */
    public boolean isUsed() {
        return !sessions.isEmpty();
    }

    /** Returns whether this cashier may be safely deleted (i.e. has no sessions). */
    public boolean isOkToDelete() {
        return sessions.isEmpty();
    }

    public List<Session> getSessions() {
        return sessions;
    }

    public void setSessions(ArrayList<Session> sessions) {
        this.sessions = sessions;
    }

    @Override
    public String toString() {
        return person.getName();
    }
}
