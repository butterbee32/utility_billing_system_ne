package rw.gov.utility_billing_system.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.gov.utility_billing_system.dto.request.meter.MeterRequest;
import rw.gov.utility_billing_system.dto.response.PageResponse;
import rw.gov.utility_billing_system.dto.response.meter.MeterResponse;
import rw.gov.utility_billing_system.entity.Meter;
import rw.gov.utility_billing_system.enums.AuditAction;
import rw.gov.utility_billing_system.enums.Status;
import rw.gov.utility_billing_system.exception.BadRequestException;
import rw.gov.utility_billing_system.exception.DuplicateResourceException;
import rw.gov.utility_billing_system.exception.ResourceNotFoundException;
import rw.gov.utility_billing_system.mapper.EntityMapper;
import rw.gov.utility_billing_system.repository.MeterRepository;

@Service
@RequiredArgsConstructor
public class MeterService {

    private final MeterRepository meterRepository;
    private final CustomerService customerService;
    private final AuditLogService auditLogService;

    @Transactional
    public MeterResponse create(MeterRequest request) {
        if (meterRepository.existsByMeterNumber(request.getMeterNumber())) {
            throw new DuplicateResourceException("Meter number already exists");
        }
        if (request.getInstallationDate().isAfter(java.time.LocalDate.now())) {
            throw new BadRequestException("Installation date cannot be in the future");
        }
        Meter meter = Meter.builder()
                .meterNumber(request.getMeterNumber())
                .meterType(request.getMeterType())
                .installationDate(request.getInstallationDate())
                .status(request.getStatus() != null ? request.getStatus() : Status.ACTIVE)
                .customer(customerService.findById(request.getCustomerId()))
                .build();
        meterRepository.save(meter);
        auditLogService.log(AuditAction.CREATE, "Meter", meter.getId(), "Meter created");
        return EntityMapper.toMeterResponse(meter);
    }

    public MeterResponse getById(Long id) {
        return EntityMapper.toMeterResponse(findById(id));
    }

    public PageResponse<MeterResponse> getAll(Pageable pageable) {
        return PageResponse.from(meterRepository.findAll(pageable).map(EntityMapper::toMeterResponse));
    }

    public PageResponse<MeterResponse> search(String keyword, Pageable pageable) {
        Page<MeterResponse> page = meterRepository
                .findByMeterNumberContainingIgnoreCaseOrCustomerFullNamesContainingIgnoreCase(
                        keyword, keyword, pageable)
                .map(EntityMapper::toMeterResponse);
        return PageResponse.from(page);
    }

    @Transactional
    public MeterResponse update(Long id, MeterRequest request) {
        Meter meter = findById(id);
        if (!meter.getMeterNumber().equals(request.getMeterNumber())
                && meterRepository.existsByMeterNumber(request.getMeterNumber())) {
            throw new DuplicateResourceException("Meter number already exists");
        }
        meter.setMeterNumber(request.getMeterNumber());
        meter.setMeterType(request.getMeterType());
        meter.setInstallationDate(request.getInstallationDate());
        if (request.getStatus() != null) {
            meter.setStatus(request.getStatus());
        }
        meter.setCustomer(customerService.findById(request.getCustomerId()));
        meterRepository.save(meter);
        auditLogService.log(AuditAction.UPDATE, "Meter", id, "Meter updated");
        return EntityMapper.toMeterResponse(meter);
    }

    @Transactional
    public void delete(Long id) {
        Meter meter = findById(id);
        meterRepository.delete(meter);
        auditLogService.log(AuditAction.DELETE, "Meter", id, "Meter deleted");
    }

    public Meter findById(Long id) {
        return meterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Meter not found: " + id));
    }
}
