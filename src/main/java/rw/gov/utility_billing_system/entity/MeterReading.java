package rw.gov.utility_billing_system.entity;

import jakarta.persistence.*;
import lombok.*;
import rw.gov.utility_billing_system.entity.base.AuditableEntity;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "meter_readings", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"meter_id", "billing_month", "billing_year"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MeterReading extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meter_id", nullable = false)
    private Meter meter;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal previousReading;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal currentReading;

    @Column(nullable = false)
    private LocalDate readingDate;

    @Column(nullable = false)
    private int billingMonth;

    @Column(nullable = false)
    private int billingYear;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal consumption;

    @OneToOne(mappedBy = "meterReading")
    private Bill bill;
}
