package rw.gov.utility_billing_system.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Order(100)
@RequiredArgsConstructor
public class DatabaseTriggerInitializer implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        try {
            jdbcTemplate.execute("""
                    CREATE OR REPLACE FUNCTION notify_bill_generated()
                    RETURNS TRIGGER AS $func$
                    DECLARE
                        customer_name VARCHAR;
                        customer_id_val BIGINT;
                    BEGIN
                        SELECT c.id, c.full_names INTO customer_id_val, customer_name
                        FROM customers c WHERE c.id = NEW.customer_id;

                        INSERT INTO notifications (customer_id, message, type, read_status, email_sent, email_status, created_at)
                        VALUES (
                            customer_id_val,
                            'Dear ' || customer_name || ', Your ' ||
                            LPAD(NEW.billing_month::TEXT, 2, '0') || '/' || NEW.billing_year ||
                            ' utility bill of ' || NEW.total_amount || ' FRW has been successfully processed.',
                            'BILL_GENERATED',
                            'UNREAD',
                            false,
                            'NOT_REQUIRED',
                            NOW()
                        );
                        RETURN NEW;
                    END;
                    $func$ LANGUAGE plpgsql
                    """);

            jdbcTemplate.execute("DROP TRIGGER IF EXISTS trg_bill_generated_notification ON bills");
            jdbcTemplate.execute("""
                    CREATE TRIGGER trg_bill_generated_notification
                        AFTER INSERT ON bills
                        FOR EACH ROW
                        EXECUTE PROCEDURE notify_bill_generated()
                    """);

            jdbcTemplate.execute("""
                    CREATE OR REPLACE FUNCTION notify_bill_paid()
                    RETURNS TRIGGER AS $func$
                    DECLARE
                        customer_name VARCHAR;
                        customer_id_val BIGINT;
                    BEGIN
                        IF NEW.outstanding_balance = 0 AND NEW.status = 'PAID'
                           AND (OLD.outstanding_balance > 0 OR OLD.status <> 'PAID') THEN
                            SELECT c.id, c.full_names INTO customer_id_val, customer_name
                            FROM customers c WHERE c.id = NEW.customer_id;

                            INSERT INTO notifications (customer_id, message, type, read_status, email_sent, email_status, created_at)
                            VALUES (
                                customer_id_val,
                                'Dear ' || customer_name || ', Your ' ||
                                LPAD(NEW.billing_month::TEXT, 2, '0') || '/' || NEW.billing_year ||
                                ' utility bill of ' || NEW.total_amount || ' FRW has been successfully processed.',
                                'BILL_PAID',
                                'UNREAD',
                                false,
                                'NOT_REQUIRED',
                                NOW()
                            );
                        END IF;
                        RETURN NEW;
                    END;
                    $func$ LANGUAGE plpgsql
                    """);

            jdbcTemplate.execute("DROP TRIGGER IF EXISTS trg_bill_paid_notification ON bills");
            jdbcTemplate.execute("""
                    CREATE TRIGGER trg_bill_paid_notification
                        AFTER UPDATE ON bills
                        FOR EACH ROW
                        EXECUTE PROCEDURE notify_bill_paid()
                    """);

            log.info("PostgreSQL bill notification triggers initialized");
        } catch (Exception ex) {
            log.warn("Could not initialize database triggers: {}", ex.getMessage());
        }
    }
}
