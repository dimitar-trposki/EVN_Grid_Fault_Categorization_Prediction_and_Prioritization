package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.request.ManualClassificationOverrideRequest;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.ClassificationResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service.FaultClassificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/faults/{faultId}/classification")
@RequiredArgsConstructor
public class FaultClassificationController {

    private final FaultClassificationService faultClassificationService;

    @PostMapping("/classify")
    @PreAuthorize("hasAnyRole('DISPATCHER', 'OPERATOR', 'ADMIN')")
    public ResponseEntity<ClassificationResponse> classify(@PathVariable Long faultId) {
        return ResponseEntity.ok(faultClassificationService.classifyFault(faultId));
    }

    @PostMapping("/reclassify")
    @PreAuthorize("hasAnyRole('DISPATCHER', 'OPERATOR', 'ADMIN')")
    public ResponseEntity<ClassificationResponse> reclassify(@PathVariable Long faultId) {
        return ResponseEntity.ok(faultClassificationService.reclassify(faultId));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ClassificationResponse> getClassification(@PathVariable Long faultId) {
        return ResponseEntity.ok(faultClassificationService.getByFault(faultId));
    }

    @PutMapping("/override")
    @PreAuthorize("hasAnyRole('DISPATCHER', 'ADMIN')")
    public ResponseEntity<ClassificationResponse> override(
            @PathVariable Long faultId,
            @RequestBody @Valid ManualClassificationOverrideRequest request) {
        return ResponseEntity.ok(faultClassificationService.manualOverride(faultId, request));
    }
}
