package POSPD;

import java.math.BigDecimal;

/**
 * A customer payment toward a {@link Sale}: the amount applied and the amount tendered. Subclasses
 * model the tender type (cash, credit, check).
 */
public abstract class Payment {

    /** Amount of the sale this payment is applied to. */
    private BigDecimal amount;

    /** Amount actually tendered by the customer. */
    private BigDecimal amtTendered;

    public BigDecimal getAmount() {
        return this.amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void setAmount(String amount) {
        this.amount = new BigDecimal(amount);
    }

    public BigDecimal getAmtTendered() {
        return this.amtTendered;
    }

    public void setAmtTendered(String amtTendered) {
        this.amtTendered = new BigDecimal(amtTendered);
    }

    public void setAmtTendered(BigDecimal amtTendered) {
        this.amtTendered = amtTendered;
    }

    /**
     * Whether this tender counts as cash for the purpose of making change. Cash does; authorized
     * tenders (credit, check) do not.
     *
     * @return {@code true} if the tender counts as cash
     */
    public boolean countsAsCash() {
        return true;
    }
}
