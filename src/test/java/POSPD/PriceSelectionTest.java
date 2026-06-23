package POSPD;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Verifies date-based price selection, including the promotional-window expiry that was buggy. */
class PriceSelectionTest {

    /** Item 1001: regular 2.59 from 1/1/24, promo 2.29 within [9/1/24, 12/30/24]. */
    private Item turkeySandwich() {
        Item item = new Item("1001", "Turkey Sandwich");
        item.addPrice(new Price("2.59", "1/1/24"));
        item.addPrice(new PromoPrice("2.29", "9/1/24", "12/30/24"));
        return item;
    }

    @Test
    @DisplayName("before the promo starts, the regular price applies")
    void beforePromo() {
        Price p = turkeySandwich().getPriceForDate(LocalDate.of(2024, 6, 1));
        assertEquals(0, new BigDecimal("2.59").compareTo(p.getPrice()));
    }

    @Test
    @DisplayName("inside the promo window, the promo price applies")
    void duringPromo() {
        Price p = turkeySandwich().getPriceForDate(LocalDate.of(2024, 10, 1));
        assertEquals(0, new BigDecimal("2.29").compareTo(p.getPrice()));
    }

    @Test
    @DisplayName(
            "after the promo end date, the regular price applies again (regression: promos must expire)")
    void afterPromoExpires() {
        Price p = turkeySandwich().getPriceForDate(LocalDate.of(2025, 1, 1));
        assertEquals(0, new BigDecimal("2.59").compareTo(p.getPrice()));
    }

    @Test
    @DisplayName("no price effective for the date returns null and a clear error on charge")
    void noEffectivePrice() {
        Item item = turkeySandwich();
        LocalDate beforeAnyPrice = LocalDate.of(2023, 12, 31);
        assertNull(item.getPriceForDate(beforeAnyPrice));
        IllegalStateException ex =
                assertThrows(
                        IllegalStateException.class,
                        () -> item.calcAmountForDateQty(beforeAnyPrice, 1));
        assertTrue(ex.getMessage().contains("1001"));
    }

    @Test
    @DisplayName("Price.isEffective is inclusive of the effective date")
    void regularPriceBoundaryInclusive() {
        Price p = new Price("2.59", "1/1/24");
        assertTrue(p.isEffective(LocalDate.of(2024, 1, 1)));
        assertFalse(p.isEffective(LocalDate.of(2023, 12, 31)));
    }

    @Test
    @DisplayName("PromoPrice.isEffective includes both window boundaries and excludes outside")
    void promoBoundaries() {
        PromoPrice promo = new PromoPrice("2.29", "9/1/24", "12/30/24");
        assertTrue(promo.isEffective(LocalDate.of(2024, 9, 1))); // start inclusive
        assertTrue(promo.isEffective(LocalDate.of(2024, 12, 30))); // end inclusive
        assertFalse(promo.isEffective(LocalDate.of(2024, 8, 31)));
        assertFalse(promo.isEffective(LocalDate.of(2024, 12, 31)));
    }

    @Test
    @DisplayName("charge for a date and quantity uses the effective price, to cents")
    void chargeForQty() {
        BigDecimal amount = turkeySandwich().calcAmountForDateQty(LocalDate.of(2024, 10, 1), 3);
        assertEquals(0, new BigDecimal("6.87").compareTo(amount)); // 2.29 * 3
        assertEquals(2, amount.scale());
    }
}
