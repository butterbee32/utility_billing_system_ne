package rw.gov.utility_billing_system.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.gov.utility_billing_system.entity.Customer;
import rw.gov.utility_billing_system.entity.Notification;
import rw.gov.utility_billing_system.entity.User;
import rw.gov.utility_billing_system.enums.EmailStatus;
import rw.gov.utility_billing_system.enums.NotificationType;
import rw.gov.utility_billing_system.enums.ReadStatus;
import rw.gov.utility_billing_system.repository.NotificationRepository;
import rw.gov.utility_billing_system.utility.EmailService;

import java.time.LocalDateTime;
import java.util.function.BooleanSupplier;

@Service
@RequiredArgsConstructor
public class NotificationDispatchService {

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;

    @Transactional
    public Notification dispatchToCustomer(Customer customer, String message, NotificationType type,
                                           BooleanSupplier emailSender) {
        return save(customer, null, customer.getEmail(), message, type, emailSender);
    }

    @Transactional
    public Notification dispatchToUser(User user, String message, NotificationType type,
                                       BooleanSupplier emailSender) {
        return save(user.getCustomer(), user, user.getEmail(), message, type, emailSender);
    }

    private Notification save(Customer customer, User user, String email, String message,
                              NotificationType type, BooleanSupplier emailSender) {
        boolean emailRequired = emailSender != null;
        boolean sent = false;
        EmailStatus emailStatus = EmailStatus.NOT_REQUIRED;

        if (emailRequired) {
            sent = emailSender.getAsBoolean();
            emailStatus = sent ? EmailStatus.SENT : EmailStatus.FAILED;
        }

        Notification notification = Notification.builder()
                .customer(customer)
                .user(user)
                .message(message)
                .type(type)
                .readStatus(ReadStatus.UNREAD)
                .emailSent(sent)
                .emailStatus(emailStatus)
                .sentAt(emailRequired ? LocalDateTime.now() : null)
                .build();
        return notificationRepository.save(notification);
    }

    @Transactional
    public Notification dispatchInAppOnly(Customer customer, User user, String message, NotificationType type) {
        Notification notification = Notification.builder()
                .customer(customer)
                .user(user)
                .message(message)
                .type(type)
                .readStatus(ReadStatus.UNREAD)
                .emailStatus(EmailStatus.NOT_REQUIRED)
                .build();
        return notificationRepository.save(notification);
    }
}
