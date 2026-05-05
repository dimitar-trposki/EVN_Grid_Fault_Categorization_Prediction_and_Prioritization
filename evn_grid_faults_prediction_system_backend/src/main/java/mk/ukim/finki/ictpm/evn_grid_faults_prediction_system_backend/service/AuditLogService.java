package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service;

import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.User;

public interface AuditLogService {
    void log(User user, String action);
}
