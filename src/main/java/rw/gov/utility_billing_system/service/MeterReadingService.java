package rw.gov.utility_billing_system.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.gov.utility_billing_system.dto.request.reading.MeterReadingRequest;
import rw.gov.utility_billing_system.dto.response.reading.MeterReadingResponse;
import rw.gov.utility_billing_system.entity.Meter;
import rw.gov.utility_billing_system.entity.MeterReading;
import rw.gov.utility_billing_system.enums.AuditAction;
import rw.gov.utility_billing_system.enums.Status;
import rw.gov.utility_billing_system.exception.BadRequestException;
import rw.gov.utility_billing_system.exception.ResourceNotFoundException;
import rw.gov.utility_billing_system.mapper.EntityMapper;
import rw.gov.utility_billing_system.repository.MeterReadingRepository;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MeterReadingService {

    private final MeterReadingRepository meterReadingRepository;
    private final MeterService meterService;
    private final AuditLogService auditLogService;

    @Transactional
    public MeterReadingResponse create(MeterReadingRequest request) {
        Meter meter = meterService.findById(request.getMeterId());
        if (meter.getStatus() != Status.ACTIVE) {
            throw new BadRequestException("Inactive meters cannot receive readings");
        }
        if (request.getCurrentReading().compareTo(request.getPreviousReading()) <= 0) {
            throw new BadRequestException("Current reading must be greater than previous reading");
        }
        int month = request.getReadingDate().getMonthValue();
        int year = request.getReadingDate().getYear();
        if (meterReadingRepository.existsByMeterIdAndBillingMonthAndBillingYear(meter.getId(), month, year)) {
            throw new BadRequestException("Only one reading per meter per month/year is allowed");
        }

        BigDecimal consumption = request.getCurrentReading().subtract(request.getPreviousReading());
        MeterReading reading = MeterReading.builder()
                .meter(meter)
                .previousReading(request.getPreviousReading())
                .currentReading(request.getCurrentReading())
                .readingDate(request.getReadingDate())
                .billingMonth(month)
                .billingYear(year)
                .consumption(consumption)
                .build();
        meterReadingRepository.save(reading);
        auditLogService.log(AuditAction.CREATE, "MeterReading", reading.getId(), "Meter reading captured");
        return EntityMapper.toMeterReadingResponse(reading);
    }

    public MeterReadingResponse getById(Long id) {
        return EntityMapper.toMeterReadingResponse(findById(id));
    }

    public List<MeterReadingResponse> getAll() {
        return meterReadingRepository.findAll().stream()
                .map(EntityMapper::toMeterReadingResponse)
                .toList();
    }

    @Transactional
    public MeterReadingResponse update(Long id, MeterReadingRequest request) {
        MeterReading reading = findById(id);
        if (reading.getBill() != null) {
            throw new BadRequestException("Cannot update reading linked to a bill");
        }
        Meter meter = meterService.findById(request.getMeterId());
        if (meter.getStatus() != Status.ACTIVE) {
            throw new BadRequestException("Inactive meters cannot receive readings");
        }
        if (request.getCurrentReading().compareTo(request.getPreviousReading()) <= 0) {
            throw new BadRequestException("Current reading must be greater than previous reading");
        }
        int month = request.getReadingDate().getMonthValue();
        int year = request.getReadingDate().getYear();
        meterReadingRepository.findByMeterIdAndBillingMonthAndBillingYear(meter.getId(), month, year)
                .ifPresent(existing -> {
                    if (!existing.getId().equals(id)) {
                        throw new BadRequestException("Only one reading per meter per month/year is allowed");
                    }
                });

        reading.setMeter(meter);
        reading.setPreviousReading(request.getPreviousReading());
        reading.setCurrentReading(request.getCurrentReading());
        reading.setReadingDate(request.getReadingDate());
        reading.setBillingMonth(month);
        reading.setBillingYear(year);
        reading.setConsumption(request.getCurrentReading().subtract(request.getPreviousReading()));
        meterReadingRepository.save(reading);
        auditLogService.log(AuditAction.UPDATE, "MeterReading", id, "Meter reading updated");
        return EntityMapper.toMeterReadingResponse(reading);
    }

    @Transactional
    public void delete(Long id) {
        MeterReading reading = findById(id);
        if (reading.getBill() != null) {
            throw new BadRequestException("Cannot delete reading linked to a bill");
        }
        meterReadingRepository.delete(reading);
        auditLogService.log(AuditAction.DELETE, "MeterReading", id, "Meter reading deleted");
    }

    public MeterReading findById(Long id) {
        return meterReadingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Meter reading not found: " + id));
    }
}
