package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.web;

import lombok.RequiredArgsConstructor;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.request.ManualPriorityOverrideRequest;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.FaultPriorityResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service.FaultPriorityService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/faults/{faultId}/priority")
@RequiredArgsConstructor
public class FaultPriorityController {

    private final FaultPriorityService faultPriorityService;

    @PostMapping("/calculate")
//    @PreAuthorize("hasAnyRole('DISPATCHER', 'OPERATOR', 'ADMIN')")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<FaultPriorityResponse> calculate(@PathVariable Long faultId) {
        return ResponseEntity.ok(faultPriorityService.calculatePriority(faultId));
    }

    @PostMapping("/recalculate")
//    @PreAuthorize("hasAnyRole('DISPATCHER', 'MANAGER', 'ADMIN')")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<FaultPriorityResponse> recalculate(@PathVariable Long faultId) {
        return ResponseEntity.ok(faultPriorityService.recalculate(faultId));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<FaultPriorityResponse> get(@PathVariable Long faultId) {
        return ResponseEntity.ok(faultPriorityService.getByFault(faultId));
    }

    @PutMapping("/override")
//    @PreAuthorize("hasAnyRole('DISPATCHER', 'MANAGER', 'ADMIN')")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<FaultPriorityResponse> override(
            @PathVariable Long faultId,
            @RequestBody ManualPriorityOverrideRequest request) {
        return ResponseEntity.ok(faultPriorityService.manualOverride(faultId, request));
    }
}
