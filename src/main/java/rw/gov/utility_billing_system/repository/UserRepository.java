package rw.gov.utility_billing_system.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import rw.gov.utility_billing_system.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);

    Page<User> findByFullNamesContainingIgnoreCaseOrEmailContainingIgnoreCase(
            String fullNames, String email, Pageable pageable);
}
