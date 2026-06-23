package POSPD;

import java.math.BigDecimal;

/**
 * A check payment. Bank account details are sensitive: both display ({@link #toString()}) and
 * persistence use {@link #getMaskedAccountNumber()} so only the last four digits are ever exposed
 * or written to disk.
 */
public class Check extends AuthorizedPayment {

    /** Bank routing number (identifies the bank). */
    private String routingNumber;

    /** Bank account number. */
    private String accountNumber;

    /** Check serial number. */
    private String checkNumber;

    /** Creates an empty, zero-value check. */
    public Check() {
        setAmount(BigDecimal.ZERO);
        setAmtTendered(BigDecimal.ZERO);
        routingNumber = "";
        accountNumber = "";
        checkNumber = "";
    }

    /**
     * Creates a check from string inputs (as read from persistence). The routing number is set
     * separately via {@link #setRoutingNumber(String)}.
     *
     * @param amount the amount applied to the sale
     * @param amountTendered the amount tendered
     * @param accountNumber the bank account number
     * @param checkNumber the check serial number
     */
    public Check(String amount, String amountTendered, String accountNumber, String checkNumber) {
        this();
        setAmount(new BigDecimal(amount));
        setAmtTendered(new BigDecimal(amountTendered));
        this.accountNumber = accountNumber;
        this.checkNumber = checkNumber;
    }

    public String getRoutingNumber() {
        return this.routingNumber;
    }

    public void setRoutingNumber(String routingNumber) {
        this.routingNumber = routingNumber;
    }

    public String getAccountNumber() {
        return this.accountNumber;
    }

    /**
     * @return the account number with all but the last four digits masked (used for display and
     *     persistence so the full number is never exposed)
     */
    public String getMaskedAccountNumber() {
        return Credit.maskPan(accountNumber);
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getCheckNumber() {
        return this.checkNumber;
    }

    public void setCheckNumber(String checkNumber) {
        this.checkNumber = checkNumber;
    }

    @Override
    public String toString() {
        return "\n Amount: "
                + getAmount()
                + "\nRouting #: "
                + routingNumber
                + "\nAccount #: "
                + Credit.maskPan(accountNumber)
                + "\nCheck #: "
                + checkNumber;
    }
}
