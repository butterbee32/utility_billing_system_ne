package rw.gov.utility_billing_system.utility;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public final class ReferenceGenerator {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private ReferenceGenerator() {}

    public static String billReference() {
        return "BILL-" + LocalDateTime.now().format(FORMATTER) + "-" + shortId();
    }

    public static String paymentReference() {
        return "PAY-" + LocalDateTime.now().format(FORMATTER) + "-" + shortId();
    }

    private static String shortId() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
