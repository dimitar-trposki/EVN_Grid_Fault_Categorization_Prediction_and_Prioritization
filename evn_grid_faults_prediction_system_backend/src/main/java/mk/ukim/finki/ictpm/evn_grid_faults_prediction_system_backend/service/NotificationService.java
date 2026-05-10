package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service;

import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.NotificationResponseDto;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.NotificationResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.User;

import java.util.List;

public interface NotificationService {

    /** @deprecated Use sendToUser(Long, String, String, String) instead. */
    @Deprecated
    void sendToUser(User user, String message);

    /** @deprecated Use getMyNotifications(String) instead. */
    @Deprecated
    List<NotificationResponseDto> getForUser(Long userId);

    /** @deprecated Use markAsRead(Long, String) instead. */
    @Deprecated
    void markAsRead(Long notificationId);

    /**
     * Persist an IN_APP notification for a system user (internal, safe to call).
     * Future enhancement: integrate email/SMS/push channels.
     */
    void sendToUser(Long userId, String title, String message, String type);

    /**
     * Persist an IN_APP notification for a customer (internal, safe to call).
     * Future enhancement: integrate email/SMS/push channels.
     */
    void sendToCustomer(Long customerId, String title, String message, String type);

    List<NotificationResponse> getMyNotifications(String callerEmail);

    List<NotificationResponse> getMyUnread(String callerEmail);

    long countMyUnread(String callerEmail);

    void markAsRead(Long id, String callerEmail);

    void markAllMineAsRead(String callerEmail);
}
