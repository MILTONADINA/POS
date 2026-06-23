package POSPD;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.Random;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Verifies payment polymorphism, the cash-vs-authorized distinction, and PAN masking. */
class PaymentTest {

    @Test
    @DisplayName("only cash counts as cash for making change")
    void countsAsCash() {
        assertTrue(new Cash("1.00", "1.00").countsAsCash());
        assertFalse(new Credit("VISA", "4111111111111111", "1/1/2030").countsAsCash());
        assertFalse(new Check("1.00", "1.00", "123", "456").countsAsCash());
    }

    @Test
    @DisplayName("total payments sums tendered across heterogeneous payments, to cents")
    void polymorphicTotals() {
        Sale sale = new Sale("false");
        sale.addPayment(new Cash("2.00", "2.00"));
        Credit credit = new Credit("VISA", "4111111111111111", "1/1/2030");
        credit.setAmount("3.00");
        credit.setAmtTendered("3.00");
        sale.addPayment(credit);
        assertEquals(0, new BigDecimal("5.00").compareTo(sale.getTotalPayments()));
    }

    @Test
    @DisplayName("credit toString masks the PAN to its last four digits and no longer throws")
    void creditToStringMasksPan() {
        Credit credit = new Credit("VISA", "4111111111111111", "1/1/2030");
        credit.setAmount("3.00");
        String s = credit.toString();
        assertTrue(s.contains("1111"));
        assertFalse(s.contains("4111111111111111"));
    }

    @Test
    @DisplayName("PAN masking reveals only the last four digits")
    void maskPan() {
        assertEquals("************5550", Credit.maskPan("1111222244445550"));
        assertEquals("123", Credit.maskPan("123")); // too short to mask
    }

    @Test
    @DisplayName("simulated authorization is deterministic when the RNG is injected")
    void deterministicAuthorization() {
        Credit approve = new Credit();
        approve.setAuthorizationRng(
                new Random() {
                    @Override
                    public int nextInt(int bound) {
                        return 0; // 0 + 1 = 1 <= threshold -> approved
                    }
                });
        assertTrue(approve.isAuthorized());

        Credit decline = new Credit();
        decline.setAuthorizationRng(
                new Random() {
                    @Override
                    public int nextInt(int bound) {
                        return 99; // 99 + 1 = 100 > threshold -> declined
                    }
                });
        assertFalse(decline.isAuthorized());
    }
}
