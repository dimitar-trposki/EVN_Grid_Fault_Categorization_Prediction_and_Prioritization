package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.helpers;

import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.AuditLogResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.AuditLog;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.User;
import org.springframework.stereotype.Component;

@Component
public class AuditLogMapper {

    public AuditLogResponse toResponse(AuditLog log) {
        User actor = log.getUser();
        Long userId = actor != null ? actor.getId() : null;
        String userFullName = actor != null
                ? actor.getFirstName() + " " + actor.getLastName()
                : null;
        String actionType = log.getActionType() != null ? log.getActionType() : log.getAction();
        return new AuditLogResponse(
                log.getId(),
                userId,
                userFullName,
                log.getEntityName(),
                log.getEntityId(),
                actionType,
                log.getOldValue(),
                log.getNewValue(),
                log.getTimestamp()
        );
    }
}
