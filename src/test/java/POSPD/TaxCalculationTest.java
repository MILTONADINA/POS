package POSPD;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Verifies tax-rate selection by date, including the boundary that was previously off-by-one. */
class TaxCalculationTest {

    @Test
    @DisplayName("the latest effective rate on or before the date applies")
    void latestEffectiveRate() {
        TaxCategory food = new TaxCategory("Food");
        food.addTaxRate(new TaxRate(LocalDate.of(2020, 1, 1), new BigDecimal("0.05")));
        food.addTaxRate(new TaxRate(LocalDate.of(2024, 1, 1), new BigDecimal("0.07")));
        assertEquals(
                0,
                new BigDecimal("0.05").compareTo(food.getTaxRateForDate(LocalDate.of(2023, 6, 1))));
        assertEquals(
                0,
                new BigDecimal("0.07").compareTo(food.getTaxRateForDate(LocalDate.of(2024, 6, 1))));
    }

    @Test
    @DisplayName("a date before any rate yields zero tax")
    void noRateYieldsZero() {
        TaxCategory food = new TaxCategory("Food");
        food.addTaxRate(new TaxRate(LocalDate.of(2024, 1, 1), new BigDecimal("0.07")));
        assertEquals(
                0, BigDecimal.ZERO.compareTo(food.getTaxRateForDate(LocalDate.of(2023, 1, 1))));
    }

    @Test
    @DisplayName("a rate is effective on its own effective date (regression: was strict isAfter)")
    void effectiveDateInclusive() {
        TaxCategory food = new TaxCategory("Food");
        food.addTaxRate(new TaxRate(LocalDate.of(2024, 1, 1), new BigDecimal("0.07")));
        assertEquals(
                0,
                new BigDecimal("0.07").compareTo(food.getTaxRateForDate(LocalDate.of(2024, 1, 1))));
    }

    @Test
    @DisplayName("two rates with the same value but different dates both survive the set")
    void distinctRatesRetained() {
        TaxCategory food = new TaxCategory("Food");
        food.addTaxRate(new TaxRate(LocalDate.of(2020, 1, 1), new BigDecimal("0.07")));
        food.addTaxRate(new TaxRate(LocalDate.of(2024, 1, 1), new BigDecimal("0.07")));
        assertEquals(2, food.getTaxRates().size());
    }

    @Test
    @DisplayName("line-item tax is subtotal times rate, rounded to cents")
    void lineItemTax() {
        TaxCategory food = new TaxCategory("Food");
        food.addTaxRate(new TaxRate(LocalDate.of(2020, 1, 1), new BigDecimal("0.07")));
        Item item = new Item("1", "Bread");
        item.addPrice(new Price("2.00", "1/1/2020"));
        item.setTaxCategory(food);
        Sale sale = new Sale("false");
        SaleLineItem line = new SaleLineItem(sale, item, 3); // subtotal 6.00, tax 0.42
        assertEquals(0, new BigDecimal("0.42").compareTo(line.calcTax()));
    }
}
