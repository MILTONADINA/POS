package POSPD;

import java.time.LocalDate;

/**
 * A credit-card payment. The account number (PAN) is sensitive: {@link #toString()} and {@link
 * #getMaskedAcctNumber()} expose only the last four digits, and the persistence layer stores the
 * masked form rather than the full PAN.
 */
public class Credit extends AuthorizedPayment {

    /** Card brand (e.g. Visa, MC). */
    private String cardType;

    /** Card account number (PAN). */
    private String acctNumber;

    /** Card expiry date. */
    private LocalDate expireDate;

    /** Creates an empty credit payment with a sentinel expiry. */
    public Credit() {
        cardType = "";
        acctNumber = "";
        expireDate = DateUtils.parseDate("1/1/1111");
    }

    /**
     * Creates a credit payment from string inputs (as read from persistence).
     *
     * @param cardType the card brand
     * @param acctNumber the account number (may already be masked)
     * @param expireDate the expiry date in {@code M/d/yyyy} or {@code M/d/yy} form
     */
    public Credit(String cardType, String acctNumber, String expireDate) {
        this();
        this.cardType = cardType;
        this.acctNumber = acctNumber;
        this.expireDate = DateUtils.parseDate(expireDate);
    }

    public String getCardType() {
        return this.cardType;
    }

    public void setCardType(String cardType) {
        this.cardType = cardType;
    }

    public String getAcctNumber() {
        return this.acctNumber;
    }

    public void setAcctNumber(String acctNumber) {
        this.acctNumber = acctNumber;
    }

    /**
     * @return the account number with all but the last four digits masked (e.g. {@code
     *     ********5550})
     */
    public String getMaskedAcctNumber() {
        return maskPan(acctNumber);
    }

    public LocalDate getExpireDate() {
        return this.expireDate;
    }

    public void setExpireDate(LocalDate expireDate) {
        this.expireDate = expireDate;
    }

    /**
     * Masks all but the last four characters of a PAN. Already-masked or short values are returned
     * unchanged in shape (only trailing digits are ever revealed).
     */
    static String maskPan(String pan) {
        if (pan == null || pan.length() <= 4) {
            return pan == null ? "" : pan;
        }
        int visible = 4;
        StringBuilder masked = new StringBuilder();
        for (int i = 0; i < pan.length() - visible; i++) {
            masked.append('*');
        }
        masked.append(pan.substring(pan.length() - visible));
        return masked.toString();
    }

    @Override
    public String toString() {
        return "Credit "
                + cardType
                + " "
                + getMaskedAcctNumber()
                + " exp "
                + DateUtils.format(expireDate)
                + " amount "
                + getAmount();
    }
}
