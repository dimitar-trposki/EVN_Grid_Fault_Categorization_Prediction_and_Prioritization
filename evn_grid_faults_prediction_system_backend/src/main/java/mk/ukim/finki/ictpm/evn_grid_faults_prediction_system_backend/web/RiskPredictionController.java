package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.web;

import lombok.RequiredArgsConstructor;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.RiskPredictionResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service.RiskPredictionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/risk-predictions")
@RequiredArgsConstructor
public class RiskPredictionController {

    private final RiskPredictionService riskPredictionService;

    @PostMapping("/location/{locationId}")
    @PreAuthorize("hasAnyRole('DISPATCHER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<RiskPredictionResponse> predictForLocation(@PathVariable Long locationId) {
        return ResponseEntity.ok(riskPredictionService.predictForLocation(locationId));
    }

    @PostMapping("/equipment/{equipmentId}")
    @PreAuthorize("hasAnyRole('DISPATCHER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<RiskPredictionResponse> predictForEquipment(@PathVariable Long equipmentId) {
        return ResponseEntity.ok(riskPredictionService.predictForEquipment(equipmentId));
    }

    @PostMapping("/region/{regionId}")
    @PreAuthorize("hasAnyRole('MANAGER', 'ADMIN')")
    public ResponseEntity<List<RiskPredictionResponse>> predictForRegion(@PathVariable Long regionId) {
        return ResponseEntity.ok(riskPredictionService.predictForRegion(regionId));
    }

    @GetMapping("/location/{locationId}/latest")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<RiskPredictionResponse> getLatestForLocation(@PathVariable Long locationId) {
        return ResponseEntity.ok(riskPredictionService.getLatestForLocation(locationId));
    }

    @GetMapping("/equipment/{equipmentId}/latest")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<RiskPredictionResponse> getLatestForEquipment(@PathVariable Long equipmentId) {
        return ResponseEntity.ok(riskPredictionService.getLatestForEquipment(equipmentId));
    }

    @GetMapping("/location/{locationId}/history")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<RiskPredictionResponse>> getHistoryForLocation(@PathVariable Long locationId) {
        return ResponseEntity.ok(riskPredictionService.getHistoryForLocation(locationId));
    }

    @GetMapping("/high-risk")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<RiskPredictionResponse>> getHighRiskZones(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(riskPredictionService.getHighRiskZones(limit));
    }
}
