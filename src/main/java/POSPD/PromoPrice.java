package POSPD;

import java.time.LocalDate;
import java.util.Objects;

/**
 * A promotional {@link Price} that is only in effect within a bounded date window {@code
 * [effectiveDate, endDate]} (both inclusive).
 */
public class PromoPrice extends Price {

    /** Last date (inclusive) on which the promo applies. */
    private LocalDate endDate;

    /** Creates a zero promo price effective at the sentinel date {@code 1/1/1111}. */
    public PromoPrice() {
        super();
        endDate = DateUtils.parseDate("1/1/1111");
    }

    /**
     * Creates a promo price from string inputs (as read from persistence).
     *
     * @param price the promotional unit price
     * @param effectiveDate the start of the promo window (inclusive)
     * @param endDate the end of the promo window (inclusive)
     */
    public PromoPrice(String price, String effectiveDate, String endDate) {
        super(price, effectiveDate);
        this.endDate = DateUtils.parseDate(endDate);
    }

    public LocalDate getEndDate() {
        return this.endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    /**
     * Returns whether the promo is in effect on the given date — that is, on or after the effective
     * date <em>and</em> on or before the end date. (The original implementation had an
     * operator-precedence bug that ignored the end date for any date after the start, so expired
     * promos applied forever.)
     *
     * @param date the date to test
     * @return {@code true} only if {@code date} falls within {@code [effectiveDate, endDate]}
     */
    @Override
    public boolean isEffective(LocalDate date) {
        return !date.isBefore(getEffectiveDate()) && !date.isAfter(endDate);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PromoPrice other = (PromoPrice) o;
        return Objects.equals(getEffectiveDate(), other.getEffectiveDate())
                && getPrice().compareTo(other.getPrice()) == 0
                && Objects.equals(endDate, other.endDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getEffectiveDate(), getPrice().stripTrailingZeros(), endDate);
    }

    @Override
    public String toString() {
        return getPrice().toString()
                + " "
                + DateUtils.format(getEffectiveDate())
                + " "
                + DateUtils.format(endDate);
    }
}
