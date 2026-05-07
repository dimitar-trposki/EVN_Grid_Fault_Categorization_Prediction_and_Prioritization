package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.web;

import lombok.RequiredArgsConstructor;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.request.EquipmentRequest;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.EquipmentResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.EquipmentSummaryResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.enums.EquipmentType;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service.EquipmentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/equipment")
@RequiredArgsConstructor
public class EquipmentController {

    private final EquipmentService equipmentService;

    @PostMapping
    public EquipmentResponse create(@RequestBody EquipmentRequest request) {
        return equipmentService.create(request);
    }

    @GetMapping("/{id}")
    public EquipmentResponse getById(@PathVariable Long id) {
        return equipmentService.getById(id);
    }

    @GetMapping
    public List<EquipmentResponse> getAll() {
        return equipmentService.getAll();
    }

    @PutMapping("/{id}")
    public EquipmentResponse update(@PathVariable Long id,
                                    @RequestBody EquipmentRequest request) {
        return equipmentService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        equipmentService.delete(id);
    }

    @GetMapping("/location/{locationId}")
    public List<EquipmentResponse> getByLocation(@PathVariable Long locationId) {
        return equipmentService.getByLocationId(locationId);
    }

    @GetMapping("/type/{type}")
    public List<EquipmentResponse> getByType(@PathVariable EquipmentType type) {
        return equipmentService.getByType(type);
    }

    @GetMapping("/filter")
    public List<EquipmentResponse> getByLocationAndType(
            @RequestParam Long locationId,
            @RequestParam EquipmentType type) {
        return equipmentService.getByLocationAndType(locationId, type);
    }

    @GetMapping("/name")
    public EquipmentResponse getByName(@RequestParam String name) {
        return equipmentService.getByName(name);
    }

    @GetMapping("/summary/location/{locationId}")
    public List<EquipmentSummaryResponse> getSummaries(@PathVariable Long locationId) {
        return equipmentService.getSummariesByLocation(locationId);
    }
}