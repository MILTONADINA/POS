package POSPD;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.TreeSet;

/**
 * A named tax category (e.g. {@code Food}, {@code Beverage}) holding a history of dated tax rates.
 */
public class TaxCategory {

    /** The category name. */
    private String category;

    /** Dated tax rates, ordered by effective date. */
    private TreeSet<TaxRate> taxRates;

    /** Creates an empty, unnamed tax category. */
    public TaxCategory() {
        category = "";
        taxRates = new TreeSet<>();
    }

    /**
     * Creates a named tax category.
     *
     * @param category the category name
     */
    public TaxCategory(String category) {
        this();
        this.category = category;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public TreeSet<TaxRate> getTaxRates() {
        return taxRates;
    }

    /**
     * Returns the rate in effect on the given date — the latest-dated rate whose effective date is
     * on or before {@code date} — or {@link BigDecimal#ZERO} if none applies.
     *
     * @param date the date to evaluate
     * @return the effective fractional tax rate
     */
    public BigDecimal getTaxRateForDate(LocalDate date) {
        BigDecimal result = BigDecimal.ZERO;
        for (TaxRate t : taxRates) {
            if (t.isEffective(date)) {
                result = t.getTaxRate();
            }
        }
        return result;
    }

    /**
     * Adds a tax rate to this category.
     *
     * @param taxRate the rate to add
     */
    public void addTaxRate(TaxRate taxRate) {
        taxRates.add(taxRate);
    }

    /**
     * Removes a tax rate from this category.
     *
     * @param taxRate the rate to remove
     */
    public void removeTaxRate(TaxRate taxRate) {
        taxRates.remove(taxRate);
    }

    @Override
    public String toString() {
        return category;
    }

    /** Returns whether this category has any rates that would block deletion. */
    public boolean isUsed() {
        return false;
    }
}
