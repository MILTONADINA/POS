package POSPD;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Verifies the monetary arithmetic of a {@link Sale}: subtotal, tax, total, change, sufficiency.
 */
class SaleTest {

    /** Builds a taxable sale: Coke @0.50 x1 in Soda(5%), Skittles @0.25 x4 in Candy(2%). */
    private Sale taxableSale() {
        TaxCategory soda = new TaxCategory("Soda");
        soda.addTaxRate(new TaxRate(LocalDate.of(2000, 1, 1), new BigDecimal("0.05")));
        TaxCategory candy = new TaxCategory("Candy");
        candy.addTaxRate(new TaxRate(LocalDate.of(2000, 1, 1), new BigDecimal("0.02")));

        Item coke = new Item("Coke", "Coke");
        coke.addPrice(new Price("0.50", "1/1/2000"));
        coke.setTaxCategory(soda);
        Item skittles = new Item("Skittles", "Skittles");
        skittles.addPrice(new Price("0.25", "1/1/2000"));
        skittles.setTaxCategory(candy);

        Sale sale = new Sale("false");
        new SaleLineItem(sale, coke, 1);
        new SaleLineItem(sale, skittles, 4);
        return sale;
    }

    @Test
    @DisplayName("subtotal is the sum of line totals, to cents")
    void subtotal() {
        // 0.50*1 + 0.25*4 = 1.50
        assertEquals(0, new BigDecimal("1.50").compareTo(taxableSale().calcSubTotal()));
        assertEquals(2, taxableSale().calcSubTotal().scale());
    }

    @Test
    @DisplayName("tax is summed per line with HALF_UP rounding")
    void tax() {
        // Soda: 0.50*0.05 = 0.025 -> 0.03 ; Candy: 1.00*0.02 = 0.02 ; total 0.05
        assertEquals(0, new BigDecimal("0.05").compareTo(taxableSale().calcTax()));
    }

    @Test
    @DisplayName("total is subtotal plus tax")
    void total() {
        assertEquals(0, new BigDecimal("1.55").compareTo(taxableSale().calcTotal()));
        assertEquals(2, taxableSale().calcTotal().scale());
    }

    @Nested
    @DisplayName("payment and change")
    class PaymentAndChange {

        @Test
        @DisplayName("overpayment yields change and reports sufficient")
        void overpayment() {
            Sale sale = taxableSale();
            sale.addPayment(new Cash("1.55", "2.00"));
            assertTrue(sale.isPaymentEnough());
            assertEquals(0, new BigDecimal("0.45").compareTo(sale.calcChange()));
        }

        @Test
        @DisplayName("underpayment reports insufficient and never negative change")
        void underpayment() {
            Sale sale = taxableSale();
            sale.addPayment(new Cash("1.00", "1.00"));
            assertFalse(sale.isPaymentEnough());
            assertEquals(0, BigDecimal.ZERO.compareTo(sale.calcChange()));
        }

        @Test
        @DisplayName("exact payment is sufficient with zero change")
        void exact() {
            Sale sale = taxableSale();
            sale.addPayment(new Cash("1.55", "1.55"));
            assertTrue(sale.isPaymentEnough());
            assertEquals(0, BigDecimal.ZERO.compareTo(sale.calcChange()));
        }

        @Test
        @DisplayName("calcAmount caps an over-tender at the remaining balance")
        void calcAmountCaps() {
            Sale sale = taxableSale(); // total 1.55
            assertEquals(
                    0, new BigDecimal("1.55").compareTo(sale.calcAmount(new BigDecimal("5.00"))));
            assertEquals(
                    0, new BigDecimal("1.00").compareTo(sale.calcAmount(new BigDecimal("1.00"))));
        }

        @Test
        @DisplayName("calcAmount returns zero (never negative) when the sale is already overpaid")
        void calcAmountNeverNegativeWhenOverpaid() {
            Sale sale = taxableSale(); // total 1.55
            sale.addPayment(new Cash("1.55", "5.00")); // tender exceeds the total
            assertEquals(0, BigDecimal.ZERO.compareTo(sale.calcAmount(new BigDecimal("5.00"))));
        }
    }

    @Test
    @DisplayName("a tax-free sale has zero tax and total equals subtotal")
    void taxFree() {
        Sale sale = taxableSale();
        sale.setTaxFree(true);
        assertEquals(0, BigDecimal.ZERO.compareTo(sale.calcTax()));
        assertEquals(0, sale.calcSubTotal().compareTo(sale.calcTotal()));
    }

    @Test
    @DisplayName("Sale(String) parses both Y/N and true/false encodings")
    void parsesTaxFreeEncodings() {
        assertTrue(new Sale("Y").getTaxFree());
        assertTrue(new Sale("true").getTaxFree());
        assertFalse(new Sale("N").getTaxFree());
        assertFalse(new Sale("false").getTaxFree());
    }
}
