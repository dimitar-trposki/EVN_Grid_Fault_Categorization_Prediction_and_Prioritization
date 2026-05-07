package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.request.CreateRegionRequest;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.request.UpdateRegionRequest;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.RegionResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service.RegionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/regions")
@RequiredArgsConstructor
public class RegionController {

    private final RegionService regionService;

    @PostMapping
    public ResponseEntity<RegionResponse> create(@RequestBody @Valid CreateRegionRequest request) {
        return ResponseEntity.ok(regionService.create(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RegionResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(regionService.getById(id));
    }

    @GetMapping
    public ResponseEntity<List<RegionResponse>> getAll() {
        return ResponseEntity.ok(regionService.getAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<RegionResponse> update(
            @PathVariable Long id,
            @RequestBody @Valid UpdateRegionRequest request
    ) {
        return ResponseEntity.ok(regionService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        regionService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/by-name")
    public ResponseEntity<RegionResponse> getByName(@RequestParam String name) {
        return ResponseEntity.ok(regionService.findByName(name));
    }
}