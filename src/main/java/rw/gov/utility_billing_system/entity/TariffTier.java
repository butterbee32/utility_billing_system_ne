package rw.gov.utility_billing_system.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "tariff_tiers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TariffTier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tariff_id", nullable = false)
    private Tariff tariff;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal minConsumption;

    @Column(precision = 12, scale = 2)
    private BigDecimal maxConsumption;

    @Column(nullable = false, precision = 12, scale = 4)
    private BigDecimal ratePerUnit;
}
