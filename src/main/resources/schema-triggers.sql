-- PostgreSQL triggers for bill notifications and payment completion
-- Runs after Hibernate creates tables (spring.jpa.defer-datasource-initialization=true)

CREATE OR REPLACE FUNCTION notify_bill_generated()
RETURNS TRIGGER AS $$
DECLARE
    customer_name VARCHAR;
    customer_id_val BIGINT;
BEGIN
    SELECT c.id, c.full_names INTO customer_id_val, customer_name
    FROM customers c WHERE c.id = NEW.customer_id;

    INSERT INTO notifications (customer_id, message, type, read, created_at)
    VALUES (
        customer_id_val,
        'Dear ' || customer_name || ', Your ' ||
        LPAD(NEW.billing_month::TEXT, 2, '0') || '/' || NEW.billing_year ||
        ' utility bill of ' || NEW.total_amount || ' FRW has been successfully processed.',
        'BILL_GENERATED',
        false,
        NOW()
    );
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_bill_generated_notification ON bills;
CREATE TRIGGER trg_bill_generated_notification
    AFTER INSERT ON bills
    FOR EACH ROW
    EXECUTE PROCEDURE notify_bill_generated();

CREATE OR REPLACE FUNCTION notify_bill_paid()
RETURNS TRIGGER AS $$
DECLARE
    customer_name VARCHAR;
    customer_id_val BIGINT;
    bill_month INT;
    bill_year INT;
    bill_total NUMERIC;
BEGIN
    IF NEW.outstanding_balance = 0 AND NEW.status = 'PAID'
       AND (OLD.outstanding_balance > 0 OR OLD.status <> 'PAID') THEN

        SELECT c.id, c.full_names INTO customer_id_val, customer_name
        FROM customers c WHERE c.id = NEW.customer_id;

        bill_month := NEW.billing_month;
        bill_year := NEW.billing_year;
        bill_total := NEW.total_amount;

        INSERT INTO notifications (customer_id, message, type, read, created_at)
        VALUES (
            customer_id_val,
            'Dear ' || customer_name || ', Your ' ||
            LPAD(bill_month::TEXT, 2, '0') || '/' || bill_year ||
            ' utility bill of ' || bill_total || ' FRW has been successfully processed.',
            'BILL_PAID',
            false,
            NOW()
        );
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_bill_paid_notification ON bills;
CREATE TRIGGER trg_bill_paid_notification
    AFTER UPDATE ON bills
    FOR EACH ROW
    EXECUTE PROCEDURE notify_bill_paid();
