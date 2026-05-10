package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response;

import java.time.LocalDateTime;

public record NotificationResponse(
        Long id,
        String title,
        String message,
        String type,
        String channel,
        boolean isRead,
        LocalDateTime createdAt
) {}
