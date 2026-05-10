package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.repository;

import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.SystemNotification;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.enums.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SystemNotificationRepository extends JpaRepository<SystemNotification, Long> {

    List<SystemNotification> findByUserId(Long userId);

    List<SystemNotification> findByCustomerId(Long customerId);

    List<SystemNotification> findByUserIdAndNotificationStatus(Long userId, NotificationStatus status);

    List<SystemNotification> findByCustomerIdAndNotificationStatus(Long customerId, NotificationStatus status);

    long countByUserIdAndNotificationStatus(Long userId, NotificationStatus status);

    long countByCustomerIdAndNotificationStatus(Long customerId, NotificationStatus status);

    List<SystemNotification> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<SystemNotification> findByCustomerIdOrderByCreatedAtDesc(Long customerId);
}
