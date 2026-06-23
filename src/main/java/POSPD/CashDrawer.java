package POSPD;

import java.math.BigDecimal;

/**
 * The cash drawer held by a {@link Register}, tracking its current cash balance and open/closed
 * position.
 */
public class CashDrawer {

    /** Current cash balance in the drawer. */
    private BigDecimal cashAmount;

    /** Drawer position: open, closed, or other. */
    private int position;

    /** Creates a closed drawer with a zero balance. */
    public CashDrawer() {
        cashAmount = BigDecimal.ZERO;
        position = 0;
    }

    public BigDecimal getCash() {
        return cashAmount;
    }

    public int getPosition() {
        return this.position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    /**
     * Adds cash to the drawer.
     *
     * @param cash the amount to add
     */
    public void addCash(BigDecimal cash) {
        cashAmount = cashAmount.add(cash);
    }

    /**
     * Removes cash from the drawer. ({@link BigDecimal} is immutable, so the result of the
     * subtraction must be reassigned — the original code discarded it, making removal a no-op.)
     *
     * @param cash the amount to remove
     */
    public void removeCash(BigDecimal cash) {
        cashAmount = cashAmount.subtract(cash);
    }

    @Override
    public String toString() {
        return "\nCash in drawer: " + cashAmount.toString();
    }
}
