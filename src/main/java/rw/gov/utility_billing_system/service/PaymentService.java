package rw.gov.utility_billing_system.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.gov.utility_billing_system.dto.request.payment.PaymentRequest;
import rw.gov.utility_billing_system.dto.response.PageResponse;
import rw.gov.utility_billing_system.dto.response.payment.PaymentResponse;
import rw.gov.utility_billing_system.entity.Bill;
import rw.gov.utility_billing_system.entity.Payment;
import rw.gov.utility_billing_system.enums.AuditAction;
import rw.gov.utility_billing_system.enums.BillStatus;
import rw.gov.utility_billing_system.enums.NotificationType;
import rw.gov.utility_billing_system.exception.BadRequestException;
import rw.gov.utility_billing_system.exception.ResourceNotFoundException;
import rw.gov.utility_billing_system.mapper.EntityMapper;
import rw.gov.utility_billing_system.repository.BillRepository;
import rw.gov.utility_billing_system.repository.PaymentRepository;
import rw.gov.utility_billing_system.utility.EmailService;
import rw.gov.utility_billing_system.utility.ReferenceGenerator;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BillRepository billRepository;
    private final BillService billService;
    private final AuditLogService auditLogService;
    private final EmailService emailService;
    private final NotificationDispatchService notificationDispatchService;

    @Transactional
    public PaymentResponse recordPayment(PaymentRequest request) {
        Bill bill = billService.findByReference(request.getBillReference());

        if (bill.getStatus() != BillStatus.APPROVED && bill.getStatus() != BillStatus.PAID) {
            throw new BadRequestException("Bill must be approved before payment");
        }
        if (bill.getStatus() == BillStatus.PAID) {
            throw new BadRequestException("Bill is already fully paid");
        }
        if (request.getAmountPaid().compareTo(bill.getOutstandingBalance()) > 0) {
            throw new BadRequestException("Payment cannot exceed outstanding balance");
        }

        Payment payment = Payment.builder()
                .bill(bill)
                .amountPaid(request.getAmountPaid())
                .paymentMethod(request.getPaymentMethod())
                .paymentDate(request.getPaymentDate())
                .paymentReference(ReferenceGenerator.paymentReference())
                .build();
        paymentRepository.save(payment);

        BigDecimal newPaid = bill.getPaidAmount().add(request.getAmountPaid());
        BigDecimal newBalance = bill.getTotalAmount().subtract(newPaid);
        bill.setPaidAmount(newPaid);
        bill.setOutstandingBalance(newBalance);

        if (newBalance.compareTo(BigDecimal.ZERO) == 0) {
            bill.setStatus(BillStatus.PAID);
        }
        billRepository.save(bill);

        auditLogService.log(AuditAction.PAYMENT, "Payment", payment.getId(),
                "Payment of " + request.getAmountPaid() + " for bill " + bill.getBillReference());

        var customer = bill.getCustomer();
        notificationDispatchService.dispatchToCustomer(customer,
                "Payment of " + request.getAmountPaid() + " FRW received for bill " + bill.getBillReference(),
                newBalance.compareTo(BigDecimal.ZERO) == 0 ? NotificationType.BILL_PAID : NotificationType.PAYMENT_RECEIVED,
                () -> emailService.sendPaymentConfirmationEmail(
                        customer.getEmail(), customer.getFullNames(),
                        request.getAmountPaid(), bill.getBillReference()));

        return EntityMapper.toPaymentResponse(payment);
    }

    public PaymentResponse getById(Long id) {
        return EntityMapper.toPaymentResponse(findById(id));
    }

    public List<PaymentResponse> getByBillId(Long billId) {
        return paymentRepository.findByBillId(billId).stream()
                .map(EntityMapper::toPaymentResponse)
                .toList();
    }

    public PageResponse<PaymentResponse> getAll(Pageable pageable) {
        return PageResponse.from(paymentRepository.findAll(pageable).map(EntityMapper::toPaymentResponse));
    }

    public PageResponse<PaymentResponse> search(String keyword, Pageable pageable) {
        Page<PaymentResponse> page = paymentRepository
                .findByPaymentReferenceContainingIgnoreCaseOrBillBillReferenceContainingIgnoreCase(
                        keyword, keyword, pageable)
                .map(EntityMapper::toPaymentResponse);
        return PageResponse.from(page);
    }

    @Transactional
    public void delete(Long id) {
        Payment payment = findById(id);
        Bill bill = payment.getBill();
        bill.setPaidAmount(bill.getPaidAmount().subtract(payment.getAmountPaid()));
        bill.setOutstandingBalance(bill.getOutstandingBalance().add(payment.getAmountPaid()));
        if (bill.getStatus() == BillStatus.PAID) {
            bill.setStatus(BillStatus.APPROVED);
        }
        billRepository.save(bill);
        paymentRepository.delete(payment);
        auditLogService.log(AuditAction.DELETE, "Payment", id, "Payment deleted");
    }

    private Payment findById(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found: " + id));
    }
}
