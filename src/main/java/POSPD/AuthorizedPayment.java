package POSPD;

import java.security.SecureRandom;
import java.util.Random;

/**
 * A payment that must be authorized by an external party (credit card, check). Authorization here
 * is <em>simulated</em>: it approves with a fixed probability. The random source is injectable via
 * {@link #setAuthorizationRng(Random)} so the decision can be made deterministic in tests.
 */
public abstract class AuthorizedPayment extends Payment {

    /** Approval threshold out of 100 (values at or below approve), i.e. ~85% approval. */
    static final int APPROVAL_THRESHOLD = 85;

    /** Authorization code returned by the (simulated) processor. */
    private String authorizationCode;

    /** Random source for the simulated authorization decision. */
    private Random rng = new SecureRandom();

    public String getAuthorizationCode() {
        return this.authorizationCode;
    }

    public void setAuthorizationCode(String authorizationCode) {
        this.authorizationCode = authorizationCode;
    }

    /**
     * Overrides the random source used by {@link #isAuthorized()} (primarily for deterministic
     * tests).
     *
     * @param rng the random source to use
     */
    public void setAuthorizationRng(Random rng) {
        this.rng = rng;
    }

    /**
     * Simulates an authorization decision.
     *
     * @return {@code true} if the (simulated) processor approves the payment
     */
    public boolean isAuthorized() {
        return rng.nextInt(100) + 1 <= APPROVAL_THRESHOLD;
    }

    /** Authorized tenders (credit, check) are not cash for the purpose of making change. */
    @Override
    public boolean countsAsCash() {
        return false;
    }
}
