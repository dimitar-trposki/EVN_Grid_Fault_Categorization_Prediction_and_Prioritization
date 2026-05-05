package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service;

import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.NotificationResponseDto;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.User;

import java.util.List;

public interface NotificationService {
    void sendToUser(User user, String message);
    List<NotificationResponseDto> getForUser(Long userId);
    void markAsRead(Long notificationId);
}
