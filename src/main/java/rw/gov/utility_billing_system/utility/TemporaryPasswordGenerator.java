package rw.gov.utility_billing_system.utility;

import java.security.SecureRandom;

public final class TemporaryPasswordGenerator {

    private static final String CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789@#$!";
    private static final SecureRandom RANDOM = new SecureRandom();

    private TemporaryPasswordGenerator() {}

    public static String generate() {
        StringBuilder sb = new StringBuilder(12);
        sb.append((char) ('A' + RANDOM.nextInt(26)));
        sb.append((char) ('a' + RANDOM.nextInt(26)));
        sb.append((char) ('0' + RANDOM.nextInt(10)));
        sb.append("@");
        for (int i = 4; i < 12; i++) {
            sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        }
        return sb.toString();
    }
}
