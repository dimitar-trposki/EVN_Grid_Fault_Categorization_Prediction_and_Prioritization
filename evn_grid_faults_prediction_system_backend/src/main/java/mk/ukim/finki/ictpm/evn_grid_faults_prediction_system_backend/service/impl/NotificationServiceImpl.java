package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service.impl;

import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.NotificationResponseDto;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.SystemNotification;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.User;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.enums.NotificationStatus;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.repository.SystemNotificationRepository;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service.NotificationService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final SystemNotificationRepository notificationRepo;

    public NotificationServiceImpl(SystemNotificationRepository notificationRepo) {
        this.notificationRepo = notificationRepo;
    }

    @Override
    public void sendToUser(User user, String message) {
        SystemNotification notification = new SystemNotification();
        notification.setUser(user);
        notification.setMessage(message);
        notification.setNotificationStatus(NotificationStatus.UNREAD);
        notificationRepo.save(notification);
    }

    @Override
    public List<NotificationResponseDto> getForUser(Long userId) {
        return notificationRepo.findByUserId(userId).stream()
                .map(n -> new NotificationResponseDto(
                        n.getId(),
                        n.getMessage(),
                        n.getNotificationStatus()
                ))
                .toList();
    }

    @Override
    public void markAsRead(Long notificationId) {
        SystemNotification n = notificationRepo.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found: " + notificationId));
        n.setNotificationStatus(NotificationStatus.READ);
        notificationRepo.save(n);
    }
}