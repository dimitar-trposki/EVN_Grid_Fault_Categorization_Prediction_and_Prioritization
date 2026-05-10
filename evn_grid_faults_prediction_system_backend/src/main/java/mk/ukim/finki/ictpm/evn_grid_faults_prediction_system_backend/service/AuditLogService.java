package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service;

import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.AuditLogResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface AuditLogService {

    /** @deprecated Use log(String, Long, String, String, String) instead. */
    @Deprecated
    void log(User user, String action);

    /**
     * Persist an audit log entry, pulling actor from the security context.
     * Safe to call — a failed audit never blocks the calling business operation.
     */
    void log(String entityName, Long entityId, String actionType, String oldValue, String newValue);

    /**
     * Persist an audit log entry with an explicit actor override (for system-initiated actions
     * where there is no user in the security context).
     */
    void log(String entityName, Long entityId, String actionType, String oldValue, String newValue, Long userIdOverride);

    Page<AuditLogResponse> getAll(LocalDateTime from, LocalDateTime to, Pageable pageable);

    List<AuditLogResponse> getByEntity(String entityName, Long entityId);

    List<AuditLogResponse> getByUser(Long userId);
}
