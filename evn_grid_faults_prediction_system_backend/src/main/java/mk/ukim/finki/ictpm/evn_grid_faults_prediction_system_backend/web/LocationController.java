package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.request.CreateLocationRequest;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.request.UpdateLocationRequest;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.LocationResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service.LocationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    @PostMapping
    public ResponseEntity<LocationResponse> create(
            @Valid @RequestBody CreateLocationRequest request
    ) {
        return ResponseEntity.ok(locationService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<LocationResponse>> getAll() {
        return ResponseEntity.ok(locationService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<LocationResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(locationService.getById(id));
    }

    @GetMapping("/by-region/{regionId}")
    public ResponseEntity<List<LocationResponse>> getByRegion(@PathVariable Long regionId) {
        return ResponseEntity.ok(locationService.findAllByRegionId(regionId));
    }

    @GetMapping("/by-address")
    public ResponseEntity<LocationResponse> getByAddress(@RequestParam String address) {
        return ResponseEntity.ok(locationService.findByAddress(address));
    }

    @GetMapping("/by-latitude")
    public ResponseEntity<List<LocationResponse>> getByLatitude(@RequestParam Double latitude) {
        return ResponseEntity.ok(locationService.findByLatitude(latitude));
    }

    @GetMapping("/by-longitude")
    public ResponseEntity<List<LocationResponse>> getByLongitude(@RequestParam Double longitude) {
        return ResponseEntity.ok(locationService.findByLongitude(longitude));
    }

    @GetMapping("/by-coordinates")
    public ResponseEntity<LocationResponse> getByCoordinates(
            @RequestParam Double latitude,
            @RequestParam Double longitude
    ) {
        return ResponseEntity.ok(
                locationService.findByLongitudeAndLatitude(longitude, latitude)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<LocationResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateLocationRequest request
    ) {
        return ResponseEntity.ok(locationService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        locationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}