package POSPD;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Objects;

/**
 * A dated price for an {@link Item}. Prices are held in a sorted set per item, so the natural
 * ordering is a <em>total</em> order (effective date, then amount, then promo discriminator) that
 * is consistent with {@link #equals(Object)} — otherwise two distinct prices that compared equal
 * would be silently dropped by the set.
 */
public class Price implements Comparable<Price> {

    /** Money scale used for all computed amounts (cents). */
    static final int MONEY_SCALE = 2;

    /** The unit price. */
    private BigDecimal price;

    /** The date from which this price takes effect (inclusive). */
    private LocalDate effectiveDate;

    /** The item this price belongs to. */
    private Item item;

    /** Creates a zero price effective at the sentinel date {@code 1/1/1111}. */
    public Price() {
        price = BigDecimal.ZERO;
        effectiveDate = DateUtils.parseDate("1/1/1111");
    }

    /**
     * Creates a price from string inputs (as read from persistence).
     *
     * @param price the unit price
     * @param effectiveDate the effective date in {@code M/d/yyyy} or {@code M/d/yy} form
     */
    public Price(String price, String effectiveDate) {
        this.price = new BigDecimal(price);
        this.effectiveDate = DateUtils.parseDate(effectiveDate);
    }

    public BigDecimal getPrice() {
        return this.price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public LocalDate getEffectiveDate() {
        return this.effectiveDate;
    }

    public void setEffectiveDate(LocalDate effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public Item getItem() {
        return this.item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    /**
     * Returns whether this price is in effect on the given date (effective date inclusive).
     *
     * @param date the date to test
     * @return {@code true} if {@code date} is on or after the effective date
     */
    public boolean isEffective(LocalDate date) {
        return !date.isBefore(effectiveDate);
    }

    /**
     * Calculates the charge for a quantity of items, normalized to cents.
     *
     * @param quantity the number of items
     * @return {@code price * quantity} rounded HALF_UP to 2 decimal places
     */
    public BigDecimal calcAmountForQty(int quantity) {
        return price.multiply(BigDecimal.valueOf(quantity))
                .setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }

    /**
     * Total ordering consistent with {@link #equals}: by effective date, then amount, then a
     * regular-before-promo discriminator, then (for promos) end date.
     */
    @Override
    public int compareTo(Price other) {
        int cmp = this.effectiveDate.compareTo(other.effectiveDate);
        if (cmp != 0) {
            return cmp;
        }
        cmp = this.price.compareTo(other.price);
        if (cmp != 0) {
            return cmp;
        }
        boolean thisPromo = this instanceof PromoPrice;
        boolean otherPromo = other instanceof PromoPrice;
        if (thisPromo != otherPromo) {
            return thisPromo
                    ? 1
                    : -1; // a regular price sorts before a promo with the same date/amount
        }
        if (thisPromo) {
            return ((PromoPrice) this).getEndDate().compareTo(((PromoPrice) other).getEndDate());
        }
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Price other = (Price) o;
        return Objects.equals(effectiveDate, other.effectiveDate)
                && price.compareTo(other.price) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(effectiveDate, price.stripTrailingZeros());
    }

    @Override
    public String toString() {
        return price.toString() + " " + DateUtils.format(effectiveDate);
    }

    /** Prices are not referenced by historical records, so they may always be deleted. */
    public boolean isUsed() {
        return false;
    }
}
