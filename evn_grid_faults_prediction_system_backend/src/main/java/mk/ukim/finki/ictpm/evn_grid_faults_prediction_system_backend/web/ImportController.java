package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.web;

import lombok.RequiredArgsConstructor;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.ImportBatchResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.User;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service.ImportService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;

@RestController
@RequiredArgsConstructor
public class ImportController {

    private final ImportService importService;

    @PostMapping(value = "/api/v1/import/faults", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ImportBatchResponse> importFaults(@RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok(importService.importFaults(file, currentUserId()));
    }

    @PostMapping(value = "/api/v1/import/locations", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ImportBatchResponse> importLocations(@RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok(importService.importLocations(file, currentUserId()));
    }

    @PostMapping(value = "/api/v1/import/equipment", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ImportBatchResponse> importEquipment(@RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok(importService.importEquipment(file, currentUserId()));
    }

    @GetMapping("/api/v1/import/{batchId}")
//    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ImportBatchResponse> getBatchStatus(@PathVariable Long batchId) {
        return ResponseEntity.ok(importService.getBatchStatus(batchId));
    }

    private Long currentUserId() {
        return ((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
    }

//    private Long currentUserId() {
//        return ((User) Objects.requireNonNull(Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal())).getId();
//    }
}
