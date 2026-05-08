package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.request.CreateLocationRequest;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.request.UpdateLocationRequest;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.LocationResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service.LocationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/locations")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LocationResponse> create(@Valid @RequestBody CreateLocationRequest request) {
        return ResponseEntity.ok(locationService.create(request));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<LocationResponse>> getAll() {
        return ResponseEntity.ok(locationService.getAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<LocationResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(locationService.getById(id));
    }

    @GetMapping("/by-region/{regionId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<LocationResponse>> getByRegion(@PathVariable Long regionId) {
        return ResponseEntity.ok(locationService.findAllByRegionId(regionId));
    }

    @GetMapping("/by-address")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<LocationResponse> getByAddress(@RequestParam String address) {
        return ResponseEntity.ok(locationService.findByAddress(address));
    }

    @GetMapping("/by-latitude")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<LocationResponse>> getByLatitude(@RequestParam Double latitude) {
        return ResponseEntity.ok(locationService.findByLatitude(latitude));
    }

    @GetMapping("/by-longitude")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<LocationResponse>> getByLongitude(@RequestParam Double longitude) {
        return ResponseEntity.ok(locationService.findByLongitude(longitude));
    }

    @GetMapping("/by-coordinates")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<LocationResponse> getByCoordinates(
            @RequestParam Double latitude,
            @RequestParam Double longitude
    ) {
        return ResponseEntity.ok(locationService.findByLongitudeAndLatitude(longitude, latitude));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LocationResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateLocationRequest request
    ) {
        return ResponseEntity.ok(locationService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        locationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
