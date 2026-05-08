package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.web;

import lombok.RequiredArgsConstructor;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.request.ExportRequest;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.ExportBatchResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.User;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service.ExportService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RestController
@RequiredArgsConstructor
public class ExportController {

    private final ExportService exportService;

    @PostMapping("/api/v1/export/faults")
//    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN', 'DISPATCHER')")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ExportBatchResponse> exportFaults(@RequestBody ExportRequest request) {
        return ResponseEntity.ok(exportService.exportFaults(request, currentUserId()));
    }

    @PostMapping("/api/v1/export/interventions")
//    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN', 'DISPATCHER')")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ExportBatchResponse> exportInterventions(@RequestBody ExportRequest request) {
        return ResponseEntity.ok(exportService.exportInterventions(request, currentUserId()));
    }

    @PostMapping("/api/v1/export/analytics")
//    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN', 'DISPATCHER')")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ExportBatchResponse> exportAnalytics(@RequestBody ExportRequest request) {
        return ResponseEntity.ok(exportService.exportAnalytics(request, currentUserId()));
    }

    @GetMapping("/api/v1/export/{batchId}/download")
//    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN', 'DISPATCHER')")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Resource> download(@PathVariable Long batchId) {
        User user = currentUser();
        Resource resource = exportService.getFile(batchId, user);
        String filename = resource.getFilename() != null ? resource.getFilename() : "export";
        String contentType = filename.endsWith(".xlsx")
                ? "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                : "text/csv";
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }

    private Long currentUserId() {
        return currentUser().getId();
    }

    private User currentUser() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

//    private User currentUser() {
//        return (User) Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();
//    }
}
