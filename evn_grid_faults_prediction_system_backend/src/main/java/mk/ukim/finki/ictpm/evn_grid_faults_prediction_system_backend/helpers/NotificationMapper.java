package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.helpers;

import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.NotificationResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.SystemNotification;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.enums.NotificationStatus;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    public NotificationResponse toResponse(SystemNotification n) {
        return new NotificationResponse(
                n.getId(),
                n.getTitle(),
                n.getMessage(),
                n.getType(),
                n.getChannel(),
                n.getNotificationStatus() == NotificationStatus.READ,
                n.getCreatedAt()
        );
    }
}
