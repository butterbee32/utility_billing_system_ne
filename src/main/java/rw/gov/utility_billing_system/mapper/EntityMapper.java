package rw.gov.utility_billing_system.mapper;

import rw.gov.utility_billing_system.dto.response.audit.AuditLogResponse;
import rw.gov.utility_billing_system.dto.response.bill.BillResponse;
import rw.gov.utility_billing_system.dto.response.customer.CustomerResponse;
import rw.gov.utility_billing_system.dto.response.file.FileResponse;
import rw.gov.utility_billing_system.dto.response.meter.MeterResponse;
import rw.gov.utility_billing_system.dto.response.notification.NotificationResponse;
import rw.gov.utility_billing_system.dto.response.payment.PaymentResponse;
import rw.gov.utility_billing_system.dto.response.reading.MeterReadingResponse;
import rw.gov.utility_billing_system.dto.response.tariff.TariffResponse;
import rw.gov.utility_billing_system.dto.response.tariff.TariffTierResponse;
import rw.gov.utility_billing_system.dto.response.user.UserResponse;
import rw.gov.utility_billing_system.entity.*;

import java.util.List;
import java.util.stream.Collectors;

public final class EntityMapper {

    private EntityMapper() {}

    public static UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .fullNames(user.getFullNames())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .status(user.getStatus())
                .emailVerified(user.isEmailVerified())
                .mustChangePassword(user.isMustChangePassword())
                .accountLocked(user.isAccountLocked())
                .lastLoginAt(user.getLastLoginAt())
                .roles(user.getRoles())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    public static CustomerResponse toCustomerResponse(Customer customer) {
        return CustomerResponse.builder()
                .id(customer.getId())
                .fullNames(customer.getFullNames())
                .nationalId(customer.getNationalId())
                .email(customer.getEmail())
                .phoneNumber(customer.getPhoneNumber())
                .address(customer.getAddress())
                .status(customer.getStatus())
                .registrationSource(customer.getRegistrationSource())
                .accountVerified(customer.isAccountVerified())
                .userId(customer.getUser() != null ? customer.getUser().getId() : null)
                .createdAt(customer.getCreatedAt())
                .updatedAt(customer.getUpdatedAt())
                .build();
    }

    public static MeterResponse toMeterResponse(Meter meter) {
        return MeterResponse.builder()
                .id(meter.getId())
                .meterNumber(meter.getMeterNumber())
                .meterType(meter.getMeterType())
                .installationDate(meter.getInstallationDate())
                .status(meter.getStatus())
                .customerId(meter.getCustomer().getId())
                .customerName(meter.getCustomer().getFullNames())
                .createdAt(meter.getCreatedAt())
                .build();
    }

    public static MeterReadingResponse toMeterReadingResponse(MeterReading reading) {
        return MeterReadingResponse.builder()
                .id(reading.getId())
                .meterId(reading.getMeter().getId())
                .meterNumber(reading.getMeter().getMeterNumber())
                .previousReading(reading.getPreviousReading())
                .currentReading(reading.getCurrentReading())
                .readingDate(reading.getReadingDate())
                .billingMonth(reading.getBillingMonth())
                .billingYear(reading.getBillingYear())
                .consumption(reading.getConsumption())
                .createdAt(reading.getCreatedAt())
                .build();
    }

    public static TariffResponse toTariffResponse(Tariff tariff) {
        List<TariffTierResponse> tiers = tariff.getTiers().stream()
                .map(t -> TariffTierResponse.builder()
                        .id(t.getId())
                        .minConsumption(t.getMinConsumption())
                        .maxConsumption(t.getMaxConsumption())
                        .ratePerUnit(t.getRatePerUnit())
                        .build())
                .collect(Collectors.toList());

        return TariffResponse.builder()
                .id(tariff.getId())
                .name(tariff.getName())
                .meterType(tariff.getMeterType())
                .tariffType(tariff.getTariffType())
                .flatRate(tariff.getFlatRate())
                .fixedServiceCharge(tariff.getFixedServiceCharge())
                .taxRate(tariff.getTaxRate())
                .penaltyRate(tariff.getPenaltyRate())
                .version(tariff.getVersion())
                .effectiveFrom(tariff.getEffectiveFrom())
                .effectiveTo(tariff.getEffectiveTo())
                .active(tariff.isActive())
                .tiers(tiers)
                .createdAt(tariff.getCreatedAt())
                .build();
    }

    public static BillResponse toBillResponse(Bill bill) {
        return BillResponse.builder()
                .id(bill.getId())
                .billReference(bill.getBillReference())
                .customerId(bill.getCustomer().getId())
                .customerName(bill.getCustomer().getFullNames())
                .meterReadingId(bill.getMeterReading().getId())
                .billingMonth(bill.getBillingMonth())
                .billingYear(bill.getBillingYear())
                .meterType(bill.getMeterType())
                .consumptionAmount(bill.getConsumptionAmount())
                .tariffAmount(bill.getTariffAmount())
                .fixedCharge(bill.getFixedCharge())
                .taxAmount(bill.getTaxAmount())
                .penaltyAmount(bill.getPenaltyAmount())
                .totalAmount(bill.getTotalAmount())
                .paidAmount(bill.getPaidAmount())
                .outstandingBalance(bill.getOutstandingBalance())
                .status(bill.getStatus())
                .generatedAt(bill.getGeneratedAt())
                .approvedAt(bill.getApprovedAt())
                .createdAt(bill.getCreatedAt())
                .build();
    }

    public static PaymentResponse toPaymentResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .paymentReference(payment.getPaymentReference())
                .billReference(payment.getBill().getBillReference())
                .amountPaid(payment.getAmountPaid())
                .paymentMethod(payment.getPaymentMethod())
                .paymentDate(payment.getPaymentDate())
                .paymentStatus(payment.getPaymentStatus())
                .processedAt(payment.getProcessedAt())
                .createdAt(payment.getCreatedAt())
                .build();
    }

    public static NotificationResponse toNotificationResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .customerId(notification.getCustomer() != null ? notification.getCustomer().getId() : null)
                .userId(notification.getUser() != null ? notification.getUser().getId() : null)
                .message(notification.getMessage())
                .type(notification.getType())
                .readStatus(notification.getReadStatus())
                .emailSent(notification.isEmailSent())
                .emailStatus(notification.getEmailStatus())
                .sentAt(notification.getSentAt())
                .createdAt(notification.getCreatedAt())
                .build();
    }

    public static FileResponse toFileResponse(UploadedFile file) {
        return FileResponse.builder()
                .id(file.getId())
                .originalFileName(file.getOriginalFileName())
                .fileDescription(file.getFileDescription())
                .uploadStatus(file.getUploadStatus())
                .category(file.getCategory())
                .contentType(file.getContentType())
                .fileSize(file.getFileSize())
                .entityType(file.getEntityType())
                .entityId(file.getEntityId())
                .createdAt(file.getCreatedAt())
                .build();
    }

    public static AuditLogResponse toAuditLogResponse(AuditLog log) {
        return AuditLogResponse.builder()
                .id(log.getId())
                .action(log.getAction())
                .entityType(log.getEntityType())
                .entityId(log.getEntityId())
                .details(log.getDetails())
                .performedBy(log.getPerformedBy())
                .ipAddress(log.getIpAddress())
                .userAgent(log.getUserAgent())
                .createdAt(log.getCreatedAt())
                .build();
    }
}
