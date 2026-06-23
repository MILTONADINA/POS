package POSPD;

import java.util.Objects;

/** A Universal Product Code that resolves to an {@link Item}. */
public class UPC implements Comparable<UPC> {

    /** The UPC code. */
    private String uPC;

    /** The item this code identifies. */
    private Item item;

    /** Creates an empty UPC. */
    public UPC() {
        uPC = "";
    }

    /**
     * Creates a UPC bound to an item and registers itself with that item.
     *
     * @param upc the UPC code
     * @param item the item this code identifies
     */
    public UPC(String upc, Item item) {
        uPC = upc;
        this.item = item;
        this.item.addUPC(this);
    }

    public String getUPC() {
        return uPC;
    }

    public void setUPC(String upc) {
        uPC = upc;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    @Override
    public String toString() {
        return uPC;
    }

    /** UPCs are not referenced by historical records, so they may always be deleted. */
    public boolean isUsed() {
        return false;
    }

    @Override
    public int compareTo(UPC upc) {
        return this.getUPC().compareTo(upc.getUPC());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return Objects.equals(uPC, ((UPC) o).uPC);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(uPC);
    }
}
