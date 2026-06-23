package POSPD;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * A sellable item: an item number, description, a set of UPCs, a price history (regular and
 * promotional), and a tax category.
 */
public class Item {

    /** Store-assigned item number. */
    private String number;

    /** Human-readable description. */
    private String description;

    /** UPCs that resolve to this item, keyed by code. */
    private TreeMap<String, UPC> upcs;

    /** Price history, ordered by effective date. */
    private TreeSet<Price> prices;

    /** The tax category this item belongs to. */
    private TaxCategory taxCategory;

    /** Creates an empty item with an empty tax category and no prices or UPCs. */
    public Item() {
        taxCategory = new TaxCategory("");
        prices = new TreeSet<>();
        upcs = new TreeMap<>();
    }

    /**
     * Creates an item with a number and description.
     *
     * @param number the store item number
     * @param description the item description
     */
    public Item(String number, String description) {
        this();
        this.number = number;
        this.description = description;
    }

    public String getNumber() {
        return this.number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TreeMap<String, UPC> getUpcs() {
        return upcs;
    }

    public TreeSet<Price> getPrices() {
        return prices;
    }

    public TaxCategory getTaxCategory() {
        return this.taxCategory;
    }

    public void setTaxCategory(TaxCategory taxCategory) {
        this.taxCategory = taxCategory;
    }

    /**
     * Resolves and sets the tax category by name from the given store.
     *
     * @param taxCategory the category name
     * @param store the store holding the category definitions
     */
    public void setTaxCategory(String taxCategory, Store store) {
        this.taxCategory = store.getTaxCategory(taxCategory);
    }

    /**
     * Adds a price to this item's price history.
     *
     * @param price the price to add
     * @return {@code true} if it was added; {@code false} if an equal price (same effective date
     *     and amount) is already present, in which case the set is unchanged
     */
    public boolean addPrice(Price price) {
        price.setItem(this);
        return prices.add(price);
    }

    /**
     * Removes a price from this item's price history.
     *
     * @param price the price to remove
     */
    public void removePrice(Price price) {
        prices.remove(price);
    }

    /**
     * Adds a UPC to this item.
     *
     * @param upc the UPC to add
     */
    public void addUPC(UPC upc) {
        upcs.put(upc.getUPC(), upc);
    }

    /**
     * Returns whether this item carries the given UPC.
     *
     * @param upc the UPC code
     * @return {@code true} if present
     */
    public boolean hasUPC(String upc) {
        return upcs.containsKey(upc);
    }

    /**
     * Removes a UPC from this item.
     *
     * @param upc the UPC to remove
     */
    public void removeUPC(UPC upc) {
        upcs.remove(upc.getUPC());
    }

    /**
     * Returns the price in effect on the given date. The rule is explicit and independent of set
     * ordering: an in-window {@link PromoPrice} always takes precedence; otherwise the latest-dated
     * regular price applies. Among multiple effective promos, the latest-dated one wins.
     *
     * @param date the date to evaluate
     * @return the effective {@link Price}, or {@code null} if the item has no price effective on
     *     that date
     */
    public Price getPriceForDate(LocalDate date) {
        Price regular = null;
        PromoPrice promo = null;
        for (Price p : prices) {
            if (!p.isEffective(date)) {
                continue;
            }
            if (p instanceof PromoPrice) {
                if (promo == null || p.getEffectiveDate().isAfter(promo.getEffectiveDate())) {
                    promo = (PromoPrice) p;
                }
            } else if (regular == null
                    || !p.getEffectiveDate().isBefore(regular.getEffectiveDate())) {
                regular = p;
            }
        }
        return promo != null ? promo : regular;
    }

    /**
     * Returns the fractional tax rate for this item on the given date.
     *
     * @param date the date to evaluate
     * @return the fractional tax rate (0 if none applies)
     */
    public BigDecimal getTaxRate(LocalDate date) {
        return taxCategory.getTaxRateForDate(date);
    }

    /**
     * Calculates the charge for a quantity of this item on a given date.
     *
     * @param date the date used to select the effective price
     * @param quantity the quantity
     * @return the amount due, normalized to cents
     * @throws IllegalStateException if the item has no price effective on {@code date}
     */
    public BigDecimal calcAmountForDateQty(LocalDate date, int quantity) {
        Price effective = getPriceForDate(date);
        if (effective == null) {
            throw new IllegalStateException(
                    "No effective price for item " + number + " on " + DateUtils.format(date));
        }
        return effective.calcAmountForQty(quantity);
    }

    @Override
    public String toString() {
        return number + " " + description;
    }

    /** Returns whether this item has any UPCs or prices (and so is considered in use). */
    public boolean isUsed() {
        return !(upcs.isEmpty() && prices.isEmpty());
    }
}
