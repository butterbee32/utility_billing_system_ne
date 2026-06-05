package rw.gov.utility_billing_system.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rw.gov.utility_billing_system.entity.Notification;
import rw.gov.utility_billing_system.enums.ReadStatus;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    List<Notification> findByCustomerIdAndReadStatus(Long customerId, ReadStatus readStatus);

    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Notification> findByUserIdAndReadStatus(Long userId, ReadStatus readStatus);
}
