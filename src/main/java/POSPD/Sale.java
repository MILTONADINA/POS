package POSPD;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * A single sale: a set of line items, the payments tendered against it, the sale date, and whether
 * it is tax-exempt. All monetary results are normalized to two decimal places.
 */
public class Sale {

    private static final int MONEY_SCALE = 2;

    /** Payments tendered for this sale. */
    private List<Payment> payments;

    /** Line items in this sale. */
    private List<SaleLineItem> saleLineItems;

    /** When the sale occurred. */
    private LocalDateTime date;

    /** Whether the sale is tax-exempt. */
    private boolean taxFree;

    /** Creates an empty, taxable sale dated now. */
    public Sale() {
        date = LocalDateTime.now();
        payments = new ArrayList<>();
        saleLineItems = new ArrayList<>();
    }

    /**
     * Creates a sale, parsing the tax-free flag. Accepts both {@code "Y"}/{@code "N"} and {@code
     * "true"}/{@code "false"} encodings so the value round-trips regardless of source.
     *
     * @param taxFree the tax-free flag as a string
     */
    public Sale(String taxFree) {
        this();
        this.taxFree = "Y".equalsIgnoreCase(taxFree) || "true".equalsIgnoreCase(taxFree);
    }

    public LocalDateTime getDate() {
        return this.date;
    }

    public boolean getTaxFree() {
        return this.taxFree;
    }

    public void setTaxFree(boolean taxFree) {
        this.taxFree = taxFree;
    }

    /**
     * Adds a payment to this sale.
     *
     * @param payment the payment to add
     */
    public void addPayment(Payment payment) {
        payments.add(payment);
    }

    /**
     * Removes a payment from this sale.
     *
     * @param payment the payment to remove
     */
    public void removePayment(Payment payment) {
        payments.remove(payment);
    }

    /**
     * Adds a line item to this sale and back-links it.
     *
     * @param sli the line item to add
     */
    public void addSaleLineItem(SaleLineItem sli) {
        sli.setSale(this);
        saleLineItems.add(sli);
    }

    /**
     * Removes a line item from this sale.
     *
     * @param sli the line item to remove
     */
    public void removeSaleLineItem(SaleLineItem sli) {
        saleLineItems.remove(sli);
    }

    public List<SaleLineItem> getSaleLineItems() {
        return saleLineItems;
    }

    public List<Payment> getPayments() {
        return payments;
    }

    /**
     * @return the sale total (subtotal plus tax), to cents
     */
    public BigDecimal calcTotal() {
        return calcSubTotal().add(calcTax()).setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }

    /**
     * @return the sum of all line-item subtotals, to cents
     */
    public BigDecimal calcSubTotal() {
        BigDecimal result = BigDecimal.ZERO;
        for (SaleLineItem s : saleLineItems) {
            result = result.add(s.calcSubTotal());
        }
        return result.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }

    /**
     * @return the total tax for the sale (zero if tax-exempt), to cents
     */
    public BigDecimal calcTax() {
        BigDecimal result = BigDecimal.ZERO;
        if (!taxFree) {
            for (SaleLineItem s : saleLineItems) {
                result = result.add(s.calcTax());
            }
        }
        return result.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }

    /**
     * @return the total amount tendered across all payments, to cents
     */
    public BigDecimal getTotalPayments() {
        BigDecimal result = BigDecimal.ZERO;
        for (Payment p : payments) {
            result = result.add(p.getAmtTendered());
        }
        return result.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }

    /**
     * @return whether the amount tendered covers the sale total
     */
    public boolean isPaymentEnough() {
        return getTotalPayments().compareTo(calcTotal()) >= 0;
    }

    /**
     * Calculates how much of a tendered amount to apply to the remaining balance (capped at the
     * balance, so overpayment becomes change rather than an applied amount).
     *
     * @param amtTendered the amount offered
     * @return the amount actually applied, to cents
     */
    public BigDecimal calcAmount(BigDecimal amtTendered) {
        BigDecimal remaining = calcTotal().subtract(getTotalPayments());
        if (remaining.signum() < 0) {
            remaining = BigDecimal.ZERO; // already overpaid: nothing left to apply
        }
        BigDecimal result = remaining.compareTo(amtTendered) > 0 ? amtTendered : remaining;
        return result.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }

    /**
     * @return change owed to the customer (tendered minus total), to cents; never negative
     */
    public BigDecimal calcChange() {
        BigDecimal change = getTotalPayments().subtract(calcTotal());
        if (change.signum() < 0) {
            change = BigDecimal.ZERO;
        }
        return change.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }

    /**
     * @return total amount tendered across payments (unrounded sum), to cents
     */
    public BigDecimal calcAmtTendered() {
        return getTotalPayments();
    }

    /**
     * @return the sum of the applied amounts across payments, to cents
     */
    public BigDecimal calcAmount() {
        BigDecimal result = BigDecimal.ZERO;
        for (Payment p : payments) {
            result = result.add(p.getAmount());
        }
        return result.setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder("\nSale: ");
        result.append("\n  SubTotal: ")
                .append(calcSubTotal())
                .append(" Tax: ")
                .append(calcTax())
                .append(" Total: ")
                .append(calcTotal())
                .append("\n  Payment: ")
                .append(getTotalPayments())
                .append(" Change: ")
                .append(calcChange());
        for (SaleLineItem s : saleLineItems) {
            result.append(s.toString());
        }
        return result.toString();
    }
}
