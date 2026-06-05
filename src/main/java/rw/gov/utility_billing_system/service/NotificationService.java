package rw.gov.utility_billing_system.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.gov.utility_billing_system.dto.response.notification.NotificationResponse;
import rw.gov.utility_billing_system.entity.Notification;
import rw.gov.utility_billing_system.enums.ReadStatus;
import rw.gov.utility_billing_system.exception.ResourceNotFoundException;
import rw.gov.utility_billing_system.mapper.EntityMapper;
import rw.gov.utility_billing_system.repository.NotificationRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public List<NotificationResponse> getByCustomer(Long customerId) {
        return notificationRepository.findByCustomerIdOrderByCreatedAtDesc(customerId).stream()
                .map(EntityMapper::toNotificationResponse)
                .toList();
    }

    public List<NotificationResponse> getUnreadByCustomer(Long customerId) {
        return notificationRepository.findByCustomerIdAndReadStatus(customerId, ReadStatus.UNREAD).stream()
                .map(EntityMapper::toNotificationResponse)
                .toList();
    }

    public List<NotificationResponse> getByUser(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(EntityMapper::toNotificationResponse)
                .toList();
    }

    public NotificationResponse getById(Long id) {
        return EntityMapper.toNotificationResponse(findById(id));
    }

    public List<NotificationResponse> getAll() {
        return notificationRepository.findAll().stream()
                .map(EntityMapper::toNotificationResponse)
                .toList();
    }

    @Transactional
    public NotificationResponse markAsRead(Long id) {
        Notification notification = findById(id);
        notification.setReadStatus(ReadStatus.READ);
        notificationRepository.save(notification);
        return EntityMapper.toNotificationResponse(notification);
    }

    @Transactional
    public void delete(Long id) {
        notificationRepository.delete(findById(id));
    }

    private Notification findById(Long id) {
        return notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found: " + id));
    }
}
