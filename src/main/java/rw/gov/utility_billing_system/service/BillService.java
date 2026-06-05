package rw.gov.utility_billing_system.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.gov.utility_billing_system.dto.request.bill.BillGenerateRequest;
import rw.gov.utility_billing_system.dto.response.PageResponse;
import rw.gov.utility_billing_system.dto.response.bill.BillResponse;
import rw.gov.utility_billing_system.entity.Bill;
import rw.gov.utility_billing_system.entity.Customer;
import rw.gov.utility_billing_system.entity.Meter;
import rw.gov.utility_billing_system.entity.MeterReading;
import rw.gov.utility_billing_system.entity.Tariff;
import rw.gov.utility_billing_system.enums.AuditAction;
import rw.gov.utility_billing_system.enums.BillStatus;
import rw.gov.utility_billing_system.enums.NotificationType;
import rw.gov.utility_billing_system.enums.Status;
import rw.gov.utility_billing_system.exception.BadRequestException;
import rw.gov.utility_billing_system.exception.ResourceNotFoundException;
import rw.gov.utility_billing_system.mapper.EntityMapper;
import rw.gov.utility_billing_system.repository.BillRepository;
import rw.gov.utility_billing_system.utility.EmailService;
import rw.gov.utility_billing_system.utility.ReferenceGenerator;
import rw.gov.utility_billing_system.utility.TariffCalculator;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class BillService {

    private final BillRepository billRepository;
    private final MeterReadingService meterReadingService;
    private final TariffService tariffService;
    private final AuditLogService auditLogService;
    private final EmailService emailService;
    private final NotificationDispatchService notificationDispatchService;

    @Transactional
    public BillResponse generateBill(BillGenerateRequest request) {
        MeterReading reading = meterReadingService.findById(request.getMeterReadingId());
        if (reading.getBill() != null) {
            throw new BadRequestException("Bill already generated for this reading");
        }

        Meter meter = reading.getMeter();
        Customer customer = meter.getCustomer();
        if (customer.getStatus() != Status.ACTIVE) {
            throw new BadRequestException("Inactive customers cannot receive bills");
        }

        LocalDate billingDate = LocalDate.of(reading.getBillingYear(), reading.getBillingMonth(), 1);
        Tariff tariff = tariffService.findApplicableTariff(meter.getMeterType(), billingDate);

        BigDecimal consumption = reading.getConsumption();
        BigDecimal tariffAmount = TariffCalculator.calculateTariffAmount(tariff, consumption);
        BigDecimal fixedCharge = tariff.getFixedServiceCharge();
        BigDecimal subtotal = tariffAmount.add(fixedCharge);
        BigDecimal taxAmount = TariffCalculator.calculateTax(subtotal, tariff.getTaxRate());
        BigDecimal totalAmount = subtotal.add(taxAmount);

        Bill bill = Bill.builder()
                .billReference(ReferenceGenerator.billReference())
                .customer(customer)
                .meterReading(reading)
                .tariff(tariff)
                .billingMonth(reading.getBillingMonth())
                .billingYear(reading.getBillingYear())
                .meterType(meter.getMeterType())
                .consumptionAmount(consumption)
                .tariffAmount(tariffAmount)
                .fixedCharge(fixedCharge)
                .taxAmount(taxAmount)
                .penaltyAmount(BigDecimal.ZERO)
                .totalAmount(totalAmount)
                .paidAmount(BigDecimal.ZERO)
                .outstandingBalance(totalAmount)
                .status(BillStatus.PENDING_APPROVAL)
                .build();

        reading.setBill(bill);
        billRepository.save(bill);

        auditLogService.log(AuditAction.BILL_GENERATION, "Bill", bill.getId(),
                "Bill generated: " + bill.getBillReference());

        notificationDispatchService.dispatchToCustomer(customer,
                String.format("Dear %s, Your %02d/%d utility bill of %s FRW has been successfully processed.",
                        customer.getFullNames(), bill.getBillingMonth(), bill.getBillingYear(),
                        bill.getTotalAmount().toPlainString()),
                NotificationType.BILL_GENERATED,
                () -> emailService.sendBillNotificationEmail(
                        customer.getEmail(), customer.getFullNames(),
                        bill.getBillingMonth(), bill.getBillingYear(), bill.getTotalAmount()));

        return EntityMapper.toBillResponse(bill);
    }

    @Transactional
    public BillResponse approve(Long id) {
        Bill bill = findById(id);
        if (bill.getStatus() != BillStatus.PENDING_APPROVAL) {
            throw new BadRequestException("Only pending bills can be approved");
        }
        bill.setStatus(BillStatus.APPROVED);
        bill.setApprovedAt(java.time.LocalDateTime.now());
        billRepository.save(bill);
        auditLogService.log(AuditAction.APPROVE, "Bill", id, "Bill approved: " + bill.getBillReference());
        return EntityMapper.toBillResponse(bill);
    }

    @Transactional
    public BillResponse reject(Long id) {
        Bill bill = findById(id);
        if (bill.getStatus() != BillStatus.PENDING_APPROVAL) {
            throw new BadRequestException("Only pending bills can be rejected");
        }
        bill.setStatus(BillStatus.REJECTED);
        billRepository.save(bill);
        auditLogService.log(AuditAction.REJECT, "Bill", id, "Bill rejected: " + bill.getBillReference());
        return EntityMapper.toBillResponse(bill);
    }

    public BillResponse getById(Long id) {
        return EntityMapper.toBillResponse(findById(id));
    }

    public BillResponse getByReference(String reference) {
        return EntityMapper.toBillResponse(
                billRepository.findByBillReference(reference)
                        .orElseThrow(() -> new ResourceNotFoundException("Bill not found: " + reference)));
    }

    public PageResponse<BillResponse> getAll(Pageable pageable) {
        return PageResponse.from(billRepository.findAll(pageable).map(EntityMapper::toBillResponse));
    }

    public PageResponse<BillResponse> search(String keyword, Pageable pageable) {
        Page<BillResponse> page = billRepository
                .findByBillReferenceContainingIgnoreCaseOrCustomerFullNamesContainingIgnoreCase(
                        keyword, keyword, pageable)
                .map(EntityMapper::toBillResponse);
        return PageResponse.from(page);
    }

    public PageResponse<BillResponse> getByCustomer(Long customerId, Pageable pageable) {
        return PageResponse.from(
                billRepository.findByCustomerId(customerId, pageable).map(EntityMapper::toBillResponse));
    }

    @Transactional
    public void delete(Long id) {
        Bill bill = findById(id);
        if (bill.getStatus() == BillStatus.PAID) {
            throw new BadRequestException("Cannot delete a paid bill");
        }
        billRepository.delete(bill);
        auditLogService.log(AuditAction.DELETE, "Bill", id, "Bill deleted");
    }

    public Bill findById(Long id) {
        return billRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found: " + id));
    }

    public Bill findByReference(String reference) {
        return billRepository.findByBillReference(reference)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found: " + reference));
    }
}
