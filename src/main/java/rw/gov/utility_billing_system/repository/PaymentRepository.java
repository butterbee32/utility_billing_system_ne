package rw.gov.utility_billing_system.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import rw.gov.utility_billing_system.entity.Payment;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByPaymentReference(String paymentReference);

    boolean existsByPaymentReference(String paymentReference);

    List<Payment> findByBillId(Long billId);

    Page<Payment> findByPaymentReferenceContainingIgnoreCaseOrBillBillReferenceContainingIgnoreCase(
            String paymentRef, String billRef, Pageable pageable);
}
