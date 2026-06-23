package POSPD;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * A cashier's work session on a register: when it started and ended, and the sales rung during it.
 */
public class Session {

    /** The cashier working the session. */
    private Cashier cashier;

    /** The register being worked. */
    private Register register;

    /** Sales rung during the session. */
    private List<Sale> sales;

    /** When the session started. */
    private LocalDateTime startDateTime;

    /** When the session ended (null while active). */
    private LocalDateTime endDateTime;

    /** Creates a session starting now with no sales. */
    public Session() {
        startDateTime = LocalDateTime.now();
        sales = new ArrayList<>();
    }

    /**
     * Creates a session by resolving cashier and register numbers against the store (used by
     * persistence), wiring the back-references.
     *
     * @param cashier the cashier number
     * @param register the register number
     * @param store the store to resolve against
     * @throws IllegalArgumentException if the cashier or register cannot be found
     */
    public Session(String cashier, String register, Store store) {
        this();
        this.cashier = store.findCashierForNumber(cashier);
        this.register = store.getRegisters().get(register);
        if (this.cashier == null) {
            throw new IllegalArgumentException("Unknown cashier number: " + cashier);
        }
        if (this.register == null) {
            throw new IllegalArgumentException("Unknown register number: " + register);
        }
        this.cashier.addSession(this);
        this.register.addSession(this);
    }

    /**
     * Creates a session from a known cashier and register, wiring the back-references.
     *
     * @param cashier the cashier
     * @param register the register
     * @param store unused, retained for call-site compatibility
     */
    public Session(Cashier cashier, Register register, Store store) {
        this(cashier, register);
    }

    /**
     * Creates a session from a known cashier and register, wiring the back-references.
     *
     * @param cashier the cashier
     * @param register the register
     */
    public Session(Cashier cashier, Register register) {
        this();
        this.cashier = cashier;
        this.register = register;
        this.cashier.addSession(this);
        this.register.addSession(this);
    }

    public Cashier getCashier() {
        return this.cashier;
    }

    public void setCashier(Cashier cashier) {
        this.cashier = cashier;
    }

    public Register getRegister() {
        return this.register;
    }

    public void setRegister(Register register) {
        this.register = register;
    }

    public List<Sale> getSales() {
        return sales;
    }

    public LocalDateTime getStartDateTime() {
        return this.startDateTime;
    }

    public void setStartDateTime(LocalDateTime startDateTime) {
        this.startDateTime = startDateTime;
    }

    public LocalDateTime getEndDateTime() {
        return this.endDateTime;
    }

    public void setEndDateTime(LocalDateTime endDateTime) {
        this.endDateTime = endDateTime;
    }

    /**
     * Adds a sale to this session.
     *
     * @param sale the sale to add
     */
    public void addSale(Sale sale) {
        sales.add(sale);
    }

    /**
     * Removes a sale from this session.
     *
     * @param sale the sale to remove
     */
    public void removeSale(Sale sale) {
        sales.remove(sale);
    }

    /**
     * Calculates the difference between an expected cash amount and the register drawer's balance.
     *
     * @param cash the expected starting cash amount
     * @return {@code cash} minus the drawer's current balance
     */
    public BigDecimal calcCashCountDiff(BigDecimal cash) {
        return cash.subtract(register.getCashDrawer().getCash());
    }

    @Override
    public String toString() {
        StringBuilder result =
                new StringBuilder(
                        "\nSession: Cashier: "
                                + cashier.getPerson().getName()
                                + " on register: "
                                + register.getNumber());
        result.append("\nSession Start: ").append(DateUtils.format(startDateTime.toLocalDate()));
        for (Sale s : sales) {
            result.append(s.toString());
        }
        return result.toString();
    }
}
