package rw.gov.utility_billing_system.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.gov.utility_billing_system.dto.request.tariff.TariffRequest;
import rw.gov.utility_billing_system.dto.request.tariff.TariffTierRequest;
import rw.gov.utility_billing_system.dto.response.tariff.TariffResponse;
import rw.gov.utility_billing_system.entity.Tariff;
import rw.gov.utility_billing_system.entity.TariffTier;
import rw.gov.utility_billing_system.enums.AuditAction;
import rw.gov.utility_billing_system.enums.TariffType;
import rw.gov.utility_billing_system.exception.BadRequestException;
import rw.gov.utility_billing_system.exception.ResourceNotFoundException;
import rw.gov.utility_billing_system.mapper.EntityMapper;
import rw.gov.utility_billing_system.repository.TariffRepository;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TariffService {

    private final TariffRepository tariffRepository;
    private final AuditLogService auditLogService;

    @Transactional
    public TariffResponse create(TariffRequest request) {
        validateTariffRequest(request);
        int nextVersion = tariffRepository.countByMeterType(request.getMeterType()) + 1;

        deactivatePreviousTariffs(request.getMeterType(), request.getEffectiveFrom());

        Tariff tariff = Tariff.builder()
                .name(request.getName())
                .meterType(request.getMeterType())
                .tariffType(request.getTariffType())
                .flatRate(request.getFlatRate())
                .fixedServiceCharge(request.getFixedServiceCharge())
                .taxRate(request.getTaxRate())
                .penaltyRate(request.getPenaltyRate())
                .version(nextVersion)
                .effectiveFrom(request.getEffectiveFrom())
                .effectiveTo(request.getEffectiveTo())
                .active(true)
                .build();

        if (request.getTariffType() == TariffType.TIER_BASED && request.getTiers() != null) {
            for (TariffTierRequest tierReq : request.getTiers()) {
                TariffTier tier = TariffTier.builder()
                        .tariff(tariff)
                        .minConsumption(tierReq.getMinConsumption())
                        .maxConsumption(tierReq.getMaxConsumption())
                        .ratePerUnit(tierReq.getRatePerUnit())
                        .build();
                tariff.getTiers().add(tier);
            }
        }

        tariffRepository.save(tariff);
        auditLogService.log(AuditAction.CREATE, "Tariff", tariff.getId(),
                "Tariff v" + tariff.getVersion() + " created for " + tariff.getMeterType());
        return EntityMapper.toTariffResponse(tariff);
    }

    public TariffResponse getById(Long id) {
        return EntityMapper.toTariffResponse(findById(id));
    }

    public List<TariffResponse> getAll() {
        return tariffRepository.findAll().stream()
                .map(EntityMapper::toTariffResponse)
                .toList();
    }

    @Transactional
    public TariffResponse update(Long id, TariffRequest request) {
        Tariff tariff = findById(id);
        validateTariffRequest(request);
        tariff.setName(request.getName());
        tariff.setFixedServiceCharge(request.getFixedServiceCharge());
        tariff.setTaxRate(request.getTaxRate());
        tariff.setPenaltyRate(request.getPenaltyRate());
        tariff.setEffectiveTo(request.getEffectiveTo());
        tariffRepository.save(tariff);
        auditLogService.log(AuditAction.UPDATE, "Tariff", id, "Tariff updated");
        return EntityMapper.toTariffResponse(tariff);
    }

    @Transactional
    public void delete(Long id) {
        Tariff tariff = findById(id);
        tariff.setActive(false);
        tariffRepository.save(tariff);
        auditLogService.log(AuditAction.DELETE, "Tariff", id, "Tariff deactivated");
    }

    public Tariff findApplicableTariff(rw.gov.utility_billing_system.enums.MeterType meterType, LocalDate billingDate) {
        return tariffRepository.findApplicableTariff(meterType, billingDate)
                .orElseThrow(() -> new BadRequestException(
                        "No applicable tariff found for " + meterType + " on " + billingDate));
    }

    public Tariff findById(Long id) {
        return tariffRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tariff not found: " + id));
    }

    private void deactivatePreviousTariffs(rw.gov.utility_billing_system.enums.MeterType meterType, LocalDate effectiveFrom) {
        tariffRepository.findByMeterTypeOrderByVersionDesc(meterType).stream()
                .filter(Tariff::isActive)
                .forEach(t -> {
                    t.setEffectiveTo(effectiveFrom.minusDays(1));
                    t.setActive(false);
                    tariffRepository.save(t);
                });
    }

    private void validateTariffRequest(TariffRequest request) {
        if (request.getEffectiveFrom() == null) {
            throw new BadRequestException("Effective from date is required");
        }
        if (request.getTariffType() == TariffType.FLAT
                && (request.getFlatRate() == null || request.getFlatRate().signum() <= 0)) {
            throw new BadRequestException("Flat rate is required for FLAT tariff type");
        }
        if (request.getTariffType() == TariffType.TIER_BASED
                && (request.getTiers() == null || request.getTiers().isEmpty())) {
            throw new BadRequestException("At least one tier is required for TIER_BASED tariff");
        }
    }
}
