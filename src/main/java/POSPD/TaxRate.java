package POSPD;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

/**
 * A dated tax rate within a {@link TaxCategory}. Like {@link Price}, tax rates live in a sorted
 * set, so the natural ordering must be a total order (by effective date, then rate) consistent with
 * {@link #equals(Object)} to avoid silently dropping distinct rates.
 */
public class TaxRate implements Comparable<TaxRate> {

    /** The fractional tax rate (e.g. {@code 0.07} for 7%). */
    private BigDecimal taxRate;

    /** The date from which this rate takes effect (inclusive). */
    private LocalDate effectiveDate;

    /** Creates a zero rate effective at the sentinel date {@code 1/1/11}. */
    public TaxRate() {
        taxRate = BigDecimal.ZERO;
        effectiveDate = DateUtils.parseDate("1/1/11");
    }

    /**
     * Creates a tax rate from string inputs (as read from persistence).
     *
     * @param effectiveDate the effective date in {@code M/d/yyyy} or {@code M/d/yy} form
     * @param rate the fractional tax rate
     */
    public TaxRate(String effectiveDate, String rate) {
        this.taxRate = new BigDecimal(rate);
        this.effectiveDate = DateUtils.parseDate(effectiveDate);
    }

    /**
     * Creates a tax rate from typed inputs.
     *
     * @param effectiveDate the effective date
     * @param rate the fractional tax rate
     */
    public TaxRate(LocalDate effectiveDate, BigDecimal rate) {
        this.taxRate = rate;
        this.effectiveDate = effectiveDate;
    }

    public BigDecimal getTaxRate() {
        return this.taxRate;
    }

    public void setTaxRate(BigDecimal taxRate) {
        this.taxRate = taxRate;
    }

    public LocalDate getEffectiveDate() {
        return this.effectiveDate;
    }

    public void setEffectiveDate(LocalDate effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    /**
     * Returns whether this rate is in effect on the given date (effective date inclusive, matching
     * {@link Price#isEffective}).
     *
     * @param date the date to test
     * @return {@code true} if {@code date} is on or after the effective date
     */
    public boolean isEffective(LocalDate date) {
        return !date.isBefore(effectiveDate);
    }

    /** Total ordering by effective date then rate, consistent with {@link #equals}. */
    @Override
    public int compareTo(TaxRate other) {
        int cmp = this.effectiveDate.compareTo(other.effectiveDate);
        if (cmp != 0) {
            return cmp;
        }
        return this.taxRate.compareTo(other.taxRate);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TaxRate other = (TaxRate) o;
        return Objects.equals(effectiveDate, other.effectiveDate)
                && taxRate.compareTo(other.taxRate) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(effectiveDate, taxRate.stripTrailingZeros());
    }

    @Override
    public String toString() {
        return taxRate.toString() + " " + DateUtils.format(effectiveDate);
    }

    /** Returns whether this rate is future-dated (and therefore considered active/scheduled). */
    public boolean isUsed() {
        return effectiveDate.isAfter(LocalDate.now());
    }
}
