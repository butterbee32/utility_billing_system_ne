package rw.gov.utility_billing_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rw.gov.utility_billing_system.entity.OtpToken;
import rw.gov.utility_billing_system.enums.OtpType;

import java.time.LocalDateTime;
import java.util.Optional;

public interface OtpTokenRepository extends JpaRepository<OtpToken, Long> {

    Optional<OtpToken> findTopByEmailAndOtpTypeAndUsedFalseOrderByGeneratedAtDesc(String email, OtpType otpType);

    int countByEmailAndOtpTypeAndGeneratedAtAfter(String email, OtpType otpType, LocalDateTime after);
}
