package POSPD;

import java.util.ArrayList;
import java.util.List;

/** A cash register owned by the store, with its cash drawer and session history. */
public class Register {

    /** Register identifier. */
    private String number;

    /** The cash drawer for this register. */
    private CashDrawer cashDrawer;

    /** Sessions worked on this register. */
    private List<Session> sessions;

    /** Creates an empty register with a fresh cash drawer. */
    public Register() {
        number = "";
        cashDrawer = new CashDrawer();
        sessions = new ArrayList<>();
    }

    /**
     * Creates a register with the given number.
     *
     * @param number the register identifier
     */
    public Register(String number) {
        this();
        this.number = number;
    }

    /**
     * Creates a register with the given number and cash drawer.
     *
     * @param number the register identifier
     * @param cashDrawer the cash drawer
     */
    public Register(String number, CashDrawer cashDrawer) {
        this();
        this.number = number;
        this.cashDrawer = cashDrawer;
    }

    public String getNumber() {
        return this.number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public CashDrawer getCashDrawer() {
        return this.cashDrawer;
    }

    public void setCashDrawer(CashDrawer cashDrawer) {
        this.cashDrawer = cashDrawer;
    }

    public List<Session> getSessions() {
        return sessions;
    }

    public void setSessions(ArrayList<Session> sessions) {
        this.sessions = sessions;
    }

    /**
     * Adds a session to this register.
     *
     * @param session the session to add
     */
    public void addSession(Session session) {
        sessions.add(session);
    }

    @Override
    public String toString() {
        return number;
    }

    /** Returns whether this register has any recorded sessions. */
    public boolean isUsed() {
        return !sessions.isEmpty();
    }
}
