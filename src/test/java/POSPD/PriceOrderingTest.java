package POSPD;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Pins the {@link Comparable}/{@link Object#equals} contract for prices stored in a {@code
 * TreeSet}: distinct prices must not be silently dropped as "duplicates".
 */
class PriceOrderingTest {

    @Test
    @DisplayName("two prices with the same date but different amounts are both retained")
    void sameDateDifferentAmountBothRetained() {
        Item item = new Item("1", "Widget");
        item.addPrice(new Price("1.00", "1/1/24"));
        item.addPrice(new Price("2.00", "1/1/24"));
        assertEquals(2, item.getPrices().size());
    }

    @Test
    @DisplayName("a regular price and a promo price with the same date both survive")
    void regularAndPromoSameDateBothRetained() {
        Item item = new Item("1", "Widget");
        item.addPrice(new Price("2.59", "1/1/24"));
        item.addPrice(new PromoPrice("2.29", "1/1/24", "2/1/24"));
        assertEquals(2, item.getPrices().size());
    }

    @Test
    @DisplayName("truly identical prices collapse to one (consistent with equals)")
    void identicalPricesCollapse() {
        Item item = new Item("1", "Widget");
        item.addPrice(new Price("2.00", "1/1/24"));
        item.addPrice(new Price("2.00", "1/1/24"));
        assertEquals(1, item.getPrices().size());
    }
}
