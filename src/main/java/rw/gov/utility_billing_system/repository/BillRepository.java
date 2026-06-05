package rw.gov.utility_billing_system.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import rw.gov.utility_billing_system.entity.Bill;
import rw.gov.utility_billing_system.enums.BillStatus;

import java.util.List;
import java.util.Optional;

public interface BillRepository extends JpaRepository<Bill, Long> {

    Optional<Bill> findByBillReference(String billReference);

    boolean existsByBillReference(String billReference);

    List<Bill> findByCustomerId(Long customerId);

    Page<Bill> findByBillReferenceContainingIgnoreCaseOrCustomerFullNamesContainingIgnoreCase(
            String reference, String customerName, Pageable pageable);

    Page<Bill> findByStatus(BillStatus status, Pageable pageable);

    Page<Bill> findByCustomerId(Long customerId, Pageable pageable);
}
