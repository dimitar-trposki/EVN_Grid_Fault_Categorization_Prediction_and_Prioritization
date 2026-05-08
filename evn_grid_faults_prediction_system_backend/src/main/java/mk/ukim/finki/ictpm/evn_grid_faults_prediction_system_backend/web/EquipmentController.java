package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.request.EquipmentRequest;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.EquipmentResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.EquipmentSummaryResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.enums.EquipmentType;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service.EquipmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class EquipmentController {

    private final EquipmentService equipmentService;

    @PostMapping("/api/v1/equipment")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EquipmentResponse> create(@Valid @RequestBody EquipmentRequest request) {
        return ResponseEntity.ok(equipmentService.create(request));
    }

    @GetMapping("/api/v1/equipment")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<EquipmentResponse>> getAll() {
        return ResponseEntity.ok(equipmentService.getAll());
    }

    @GetMapping("/api/v1/equipment/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<EquipmentResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(equipmentService.getById(id));
    }

    @GetMapping("/api/v1/equipment/by-type")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<EquipmentSummaryResponse>> getByType(@RequestParam EquipmentType type) {
        return ResponseEntity.ok(equipmentService.getByType(type));
    }

    @PutMapping("/api/v1/equipment/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EquipmentResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody EquipmentRequest request
    ) {
        return ResponseEntity.ok(equipmentService.update(id, request));
    }

    @DeleteMapping("/api/v1/equipment/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        equipmentService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/v1/locations/{locationId}/equipment")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<EquipmentSummaryResponse>> getByLocation(@PathVariable Long locationId) {
        return ResponseEntity.ok(equipmentService.getByLocation(locationId));
    }

    @GetMapping("/api/v1/locations/{locationId}/equipment/by-type")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<EquipmentSummaryResponse>> getByLocationAndType(
            @PathVariable Long locationId,
            @RequestParam EquipmentType type
    ) {
        return ResponseEntity.ok(equipmentService.getByLocationAndType(locationId, type));
    }
}
