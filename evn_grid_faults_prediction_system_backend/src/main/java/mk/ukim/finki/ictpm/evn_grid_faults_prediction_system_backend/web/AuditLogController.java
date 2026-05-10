package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.web;

import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.AuditLogResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service.AuditLogService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/audit")
public class AuditLogController {

    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<AuditLogResponse>> getAll(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
        return ResponseEntity.ok(auditLogService.getAll(from, to, pageable));
    }

    @GetMapping("/entity/{entityName}/{entityId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AuditLogResponse>> getByEntity(@PathVariable String entityName,
                                                              @PathVariable Long entityId) {
        return ResponseEntity.ok(auditLogService.getByEntity(entityName, entityId));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AuditLogResponse>> getByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(auditLogService.getByUser(userId));
    }
}
