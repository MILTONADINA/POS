package POSPD;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * One line of a {@link Sale}: an {@link Item} and a quantity, with helpers to compute its subtotal
 * and tax for the sale's date.
 */
public class SaleLineItem {

    private static final int MONEY_SCALE = 2;

    /** The item being sold on this line. */
    private Item item;

    /** The sale this line belongs to. */
    private Sale sale;

    /** Quantity of the item. */
    private int quantity;

    /** Creates an empty line item backed by empty item and sale. */
    public SaleLineItem() {
        item = new Item();
        sale = new Sale();
    }

    /**
     * Creates a line item for a sale and adds itself to that sale.
     *
     * @param sale the parent sale
     * @param item the item being sold
     * @param quantity the quantity
     */
    public SaleLineItem(Sale sale, Item item, int quantity) {
        setQuantity(quantity);
        setItem(item);
        sale.addSaleLineItem(this);
    }

    /**
     * Creates a line item by resolving an item number against the store (used by persistence).
     *
     * @param itemNumber the item number
     * @param quantity the quantity as a string
     * @param store the store to resolve the item from
     */
    public SaleLineItem(String itemNumber, String quantity, Store store) {
        this();
        this.item = store.findItemForNumber(itemNumber);
        this.quantity = Integer.parseInt(quantity);
    }

    public Item getItem() {
        return this.item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public Sale getSale() {
        return this.sale;
    }

    public void setSale(Sale sale) {
        this.sale = sale;
    }

    public int getQuantity() {
        return this.quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    /**
     * @return the price of this line (unit price for the sale date times quantity), to cents
     */
    public BigDecimal calcSubTotal() {
        return item.calcAmountForDateQty(sale.getDate().toLocalDate(), quantity);
    }

    /**
     * @return the tax for this line (subtotal times the item's rate for the sale date), to cents
     */
    public BigDecimal calcTax() {
        return calcSubTotal()
                .multiply(item.getTaxRate(sale.getDate().toLocalDate()))
                .setScale(MONEY_SCALE, RoundingMode.HALF_UP);
    }

    @Override
    public String toString() {
        return "\n  "
                + item.getDescription()
                + " "
                + item.getNumber()
                + " "
                + quantity
                + " "
                + item.getPriceForDate(sale.getDate().toLocalDate())
                + " "
                + item.getTaxRate(sale.getDate().toLocalDate());
    }
}
