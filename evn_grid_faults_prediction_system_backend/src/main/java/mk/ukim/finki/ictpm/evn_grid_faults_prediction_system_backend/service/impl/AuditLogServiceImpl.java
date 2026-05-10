package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service.impl;

import lombok.extern.slf4j.Slf4j;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.AuditLogResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.exception.BadRequestException;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.helpers.AuditLogMapper;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.AuditLog;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.User;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.repository.AuditLogRepository;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.repository.UserRepository;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service.AuditLogService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@Transactional
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepo;
    private final UserRepository userRepo;
    private final AuditLogMapper mapper;

    public AuditLogServiceImpl(AuditLogRepository auditLogRepo,
                               UserRepository userRepo,
                               AuditLogMapper mapper) {
        this.auditLogRepo = auditLogRepo;
        this.userRepo = userRepo;
        this.mapper = mapper;
    }

    // --- Legacy deprecated method ---

    @Override
    @Deprecated
    public void log(User user, String action) {
        try {
            AuditLog entry = new AuditLog();
            entry.setUser(user);
            entry.setAction(action);
            entry.setActionType(action);
            entry.setTimestamp(LocalDateTime.now());
            auditLogRepo.save(entry);
        } catch (Exception e) {
            log.warn("Failed to persist legacy audit log: {}", e.getMessage());
        }
    }

    // --- New log methods ---

    @Override
    public void log(String entityName, Long entityId, String actionType, String oldValue, String newValue) {
        log(entityName, entityId, actionType, oldValue, newValue, null);
    }

    @Override
    public void log(String entityName, Long entityId, String actionType, String oldValue, String newValue,
                    Long userIdOverride) {
        try {
            User actor = resolveActor(userIdOverride);
            AuditLog entry = new AuditLog();
            entry.setEntityName(entityName);
            entry.setEntityId(entityId);
            entry.setActionType(actionType);
            entry.setAction(actionType != null && actionType.length() <= 50
                    ? actionType
                    : (actionType != null ? actionType.substring(0, 50) : "UNKNOWN"));
            entry.setOldValue(oldValue);
            entry.setNewValue(newValue);
            entry.setUser(actor);
            entry.setTimestamp(LocalDateTime.now());
            auditLogRepo.save(entry);
        } catch (Exception e) {
            log.warn("Failed to persist audit log [entity={}, id={}, action={}]: {}",
                    entityName, entityId, actionType, e.getMessage());
        }
    }

    // --- Query methods ---

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogResponse> getAll(LocalDateTime from, LocalDateTime to, Pageable pageable) {
        if (from != null && to != null) {
            if (!from.isBefore(to)) {
                throw new BadRequestException("'from' must be before 'to'");
            }
            if (from.plusDays(90).isBefore(to)) {
                throw new BadRequestException("Date range must not exceed 90 days");
            }
            return auditLogRepo.findByTimestampBetween(from, to, pageable).map(mapper::toResponse);
        }
        return auditLogRepo.findAll(pageable).map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogResponse> getByEntity(String entityName, Long entityId) {
        return auditLogRepo.findByEntityNameAndEntityId(entityName, entityId).stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogResponse> getByUser(Long userId) {
        return auditLogRepo.findByUserId(userId).stream()
                .map(mapper::toResponse)
                .toList();
    }

    // --- Private helpers ---

    private User resolveActor(Long userIdOverride) {
        if (userIdOverride != null) {
            return userRepo.findById(userIdOverride).orElse(null);
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        if (auth.getPrincipal() instanceof User u) {
            return u;
        }
        return null;
    }
}
