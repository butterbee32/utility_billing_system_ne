package rw.gov.utility_billing_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rw.gov.utility_billing_system.entity.MeterReading;

import java.util.Optional;

public interface MeterReadingRepository extends JpaRepository<MeterReading, Long> {

    boolean existsByMeterIdAndBillingMonthAndBillingYear(Long meterId, int month, int year);

    Optional<MeterReading> findByMeterIdAndBillingMonthAndBillingYear(Long meterId, int month, int year);
}
