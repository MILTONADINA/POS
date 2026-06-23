package POSPD;

import java.util.ArrayList;
import java.util.TreeMap;

/**
 * The aggregate root: a store and everything it owns — cashiers, registers, tax categories, items,
 * UPCs, and the history of work sessions.
 */
public class Store {

    /** Store identifier. */
    private String number;

    /** Store name. */
    private String name;

    /** Cashiers keyed by employee number. */
    private TreeMap<String, Cashier> cashiers;

    /** Work sessions, in chronological order. */
    private ArrayList<Session> sessions;

    /** Registers keyed by register number. */
    private TreeMap<String, Register> registers;

    /** Tax categories keyed by name. */
    private TreeMap<String, TaxCategory> taxCategories;

    /** UPCs keyed by code. */
    private TreeMap<String, UPC> upcs;

    /** Items keyed by item number. */
    private TreeMap<String, Item> items;

    /** Creates an empty store. */
    public Store() {
        name = "";
        number = "";
        items = new TreeMap<>();
        upcs = new TreeMap<>();
        taxCategories = new TreeMap<>();
        registers = new TreeMap<>();
        sessions = new ArrayList<>();
        cashiers = new TreeMap<>();
    }

    /**
     * Creates a named, numbered store.
     *
     * @param name the store name
     * @param number the store identifier
     */
    public Store(String name, String number) {
        this();
        this.number = number;
        this.name = name;
    }

    public String getNumber() {
        return this.number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TaxCategory getTaxCategory(String taxCategory) {
        return taxCategories.get(taxCategory);
    }

    public TreeMap<String, TaxCategory> getTaxCategories() {
        return taxCategories;
    }

    public ArrayList<Session> getSessions() {
        return sessions;
    }

    public TreeMap<String, Register> getRegisters() {
        return registers;
    }

    public TreeMap<String, Cashier> getCashiers() {
        return cashiers;
    }

    public TreeMap<String, Item> getItems() {
        return items;
    }

    /**
     * Adds an item, keyed by item number.
     *
     * @param item the item to add
     */
    public void addItem(Item item) {
        items.put(item.getNumber(), item);
    }

    /**
     * Removes an item.
     *
     * @param item the item to remove
     */
    public void removeItem(Item item) {
        items.remove(item.getNumber());
    }

    /**
     * Adds a register, keyed by register number.
     *
     * @param register the register to add
     */
    public void addRegister(Register register) {
        registers.put(register.getNumber(), register);
    }

    /**
     * Removes a register.
     *
     * @param register the register to remove
     */
    public void removeRegister(Register register) {
        registers.remove(register.getNumber());
    }

    /**
     * Adds a UPC, keyed by code.
     *
     * @param upc the UPC to add
     */
    public void addUPC(UPC upc) {
        upcs.put(upc.getUPC(), upc);
    }

    /**
     * Removes a UPC.
     *
     * @param upc the UPC to remove
     */
    public void removeUPC(UPC upc) {
        upcs.remove(upc.getUPC());
    }

    /**
     * Adds a cashier, keyed by employee number.
     *
     * @param cashier the cashier to add
     */
    public void addCashier(Cashier cashier) {
        cashiers.put(cashier.getNumber(), cashier);
    }

    /**
     * Removes a cashier.
     *
     * @param cashier the cashier to remove
     */
    public void removeCashier(Cashier cashier) {
        cashiers.remove(cashier.getNumber());
    }

    /**
     * Adds a session to the store's history.
     *
     * @param session the session to add
     */
    public void addSession(Session session) {
        sessions.add(session);
    }

    /**
     * Removes a session from the store's history and detaches it from its cashier and register so
     * the back-references cannot go stale.
     *
     * @param session the session to remove
     */
    public void removeSession(Session session) {
        sessions.remove(session);
        if (session.getCashier() != null) {
            session.getCashier().removeSession(session);
        }
        if (session.getRegister() != null) {
            session.getRegister().getSessions().remove(session);
        }
    }

    /**
     * Finds an item by one of its UPCs.
     *
     * @param upc the UPC code
     * @return the matching item, or {@code null} if none
     */
    public Item findItemForUPC(String upc) {
        Item result = null;
        for (Item i : items.values()) {
            if (i.hasUPC(upc)) {
                result = i;
            }
        }
        return result;
    }

    /**
     * Finds an item by its item number.
     *
     * @param number the item number
     * @return the matching item, or {@code null} if none
     */
    public Item findItemForNumber(String number) {
        return items.get(number);
    }

    /**
     * Finds a cashier by employee number.
     *
     * @param number the cashier number
     * @return the matching cashier, or {@code null} if none
     */
    public Cashier findCashierForNumber(String number) {
        return cashiers.get(number);
    }

    /**
     * Adds a tax category, keyed by name.
     *
     * @param taxCategory the category to add
     */
    public void addTaxCategory(TaxCategory taxCategory) {
        taxCategories.put(taxCategory.getCategory(), taxCategory);
    }

    /**
     * Removes a tax category.
     *
     * @param taxCategory the category to remove
     */
    public void removeTaxCategory(TaxCategory taxCategory) {
        taxCategories.remove(taxCategory.getCategory());
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder("\nStore name: " + name + "\nCashiers: ");
        for (Cashier c : cashiers.values()) {
            result.append(c.toString());
        }
        result.append("\nRegisters: ");
        for (Register r : registers.values()) {
            result.append(r.toString());
        }
        result.append("\nItems: ");
        for (Item i : items.values()) {
            result.append("\n").append(i.toString());
        }
        result.append("\nSessions: ");
        for (Session s : sessions) {
            result.append(s.toString());
        }
        return result.toString();
    }
}
