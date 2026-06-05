package rw.gov.utility_billing_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rw.gov.utility_billing_system.entity.Tariff;
import rw.gov.utility_billing_system.enums.MeterType;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TariffRepository extends JpaRepository<Tariff, Long> {

    @Query("SELECT t FROM Tariff t WHERE t.meterType = :meterType AND t.active = true " +
           "AND t.effectiveFrom <= :billingDate AND (t.effectiveTo IS NULL OR t.effectiveTo >= :billingDate) " +
           "ORDER BY t.version DESC")
    Optional<Tariff> findApplicableTariff(@Param("meterType") MeterType meterType,
                                        @Param("billingDate") LocalDate billingDate);

    List<Tariff> findByMeterTypeOrderByVersionDesc(MeterType meterType);

    int countByMeterType(MeterType meterType);
}
