package rw.gov.utility_billing_system.utility;

import java.security.SecureRandom;

public final class OtpGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();

    private OtpGenerator() {}

    public static String generate() {
        return String.format("%06d", RANDOM.nextInt(1_000_000));
    }
}
