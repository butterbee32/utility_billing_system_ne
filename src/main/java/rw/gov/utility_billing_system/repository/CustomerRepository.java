package rw.gov.utility_billing_system.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import rw.gov.utility_billing_system.entity.Customer;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long> {

    boolean existsByNationalId(String nationalId);

    boolean existsByEmail(String email);

    Optional<Customer> findByNationalId(String nationalId);

    Optional<Customer> findByEmail(String email);

    Optional<Customer> findByUserId(Long userId);

    Page<Customer> findByFullNamesContainingIgnoreCaseOrNationalIdContainingIgnoreCaseOrEmailContainingIgnoreCase(
            String fullNames, String nationalId, String email, Pageable pageable);
}
