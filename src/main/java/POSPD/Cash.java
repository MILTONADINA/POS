package POSPD;

import java.math.BigDecimal;

/** A cash payment toward a sale. */
public class Cash extends Payment {

    /** Creates a zero cash payment. */
    public Cash() {
        setAmount(BigDecimal.ZERO);
        setAmtTendered(BigDecimal.ZERO);
    }

    /**
     * Creates a cash payment from string inputs (as read from persistence).
     *
     * @param amount the amount applied to the sale
     * @param amtTendered the amount tendered by the customer
     */
    public Cash(String amount, String amtTendered) {
        setAmount(new BigDecimal(amount));
        setAmtTendered(new BigDecimal(amtTendered));
    }

    @Override
    public String toString() {
        return getAmtTendered().toString() + " paid out of " + getAmount().toString();
    }
}
