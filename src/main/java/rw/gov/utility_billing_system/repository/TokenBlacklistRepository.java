package rw.gov.utility_billing_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rw.gov.utility_billing_system.entity.TokenBlacklist;

public interface TokenBlacklistRepository extends JpaRepository<TokenBlacklist, Long> {

    boolean existsByToken(String token);
}
