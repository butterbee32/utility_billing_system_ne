package rw.gov.utility_billing_system.utility;

import rw.gov.utility_billing_system.entity.Tariff;
import rw.gov.utility_billing_system.entity.TariffTier;
import rw.gov.utility_billing_system.enums.TariffType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;

public final class TariffCalculator {

    private TariffCalculator() {}

    public static BigDecimal calculateTariffAmount(Tariff tariff, BigDecimal consumption) {
        if (tariff.getTariffType() == TariffType.FLAT) {
            return consumption.multiply(tariff.getFlatRate()).setScale(2, RoundingMode.HALF_UP);
        }
        return calculateTierAmount(tariff, consumption);
    }

    private static BigDecimal calculateTierAmount(Tariff tariff, BigDecimal consumption) {
        BigDecimal remaining = consumption;
        BigDecimal total = BigDecimal.ZERO;

        var sortedTiers = tariff.getTiers().stream()
                .sorted(Comparator.comparing(TariffTier::getMinConsumption))
                .toList();

        for (TariffTier tier : sortedTiers) {
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }
            BigDecimal tierMax = tier.getMaxConsumption() != null
                    ? tier.getMaxConsumption().subtract(tier.getMinConsumption())
                    : remaining;
            BigDecimal unitsInTier = remaining.min(tierMax);
            total = total.add(unitsInTier.multiply(tier.getRatePerUnit()));
            remaining = remaining.subtract(unitsInTier);
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal calculateTax(BigDecimal subtotal, BigDecimal taxRate) {
        return subtotal.multiply(taxRate)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }
}
