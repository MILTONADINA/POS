package POSPD;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Verifies the cash drawer balance, including the removal that was previously a no-op. */
class CashDrawerTest {

    @Test
    @DisplayName(
            "adding and removing cash updates the balance (regression: removeCash was a no-op)")
    void addAndRemove() {
        CashDrawer drawer = new CashDrawer();
        drawer.addCash(new BigDecimal("10.00"));
        drawer.removeCash(new BigDecimal("3.00"));
        assertEquals(0, new BigDecimal("7.00").compareTo(drawer.getCash()));
    }

    @Test
    @DisplayName("session cash-count difference reflects the drawer balance")
    void cashCountDiff() {
        Register register = new Register("1");
        register.getCashDrawer().addCash(new BigDecimal("100.00"));
        Cashier cashier = new Cashier("1", new Person(), "pw12345");
        Session session = new Session(cashier, register);
        // expected 120 in drawer, actual 100 -> diff 20
        assertEquals(
                0,
                new BigDecimal("20.00")
                        .compareTo(session.calcCashCountDiff(new BigDecimal("120.00"))));
    }
}
