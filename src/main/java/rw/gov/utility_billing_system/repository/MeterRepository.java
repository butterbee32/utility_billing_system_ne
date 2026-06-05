package rw.gov.utility_billing_system.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import rw.gov.utility_billing_system.entity.Meter;
import rw.gov.utility_billing_system.enums.MeterType;

import java.util.List;
import java.util.Optional;

public interface MeterRepository extends JpaRepository<Meter, Long> {

    boolean existsByMeterNumber(String meterNumber);

    Optional<Meter> findByMeterNumber(String meterNumber);

    List<Meter> findByCustomerId(Long customerId);

    Page<Meter> findByMeterNumberContainingIgnoreCaseOrCustomerFullNamesContainingIgnoreCase(
            String meterNumber, String customerName, Pageable pageable);

    Page<Meter> findByMeterType(MeterType meterType, Pageable pageable);
}
