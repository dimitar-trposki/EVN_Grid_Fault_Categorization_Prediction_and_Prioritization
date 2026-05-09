package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.web;

import lombok.RequiredArgsConstructor;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.WeatherDataResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.exception.BadRequestException;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service.WeatherService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/weather")
@RequiredArgsConstructor
public class WeatherController {

    private final WeatherService weatherService;

    @PostMapping("/fetch/{locationId}")
//    @PreAuthorize("hasAnyRole('DISPATCHER', 'MANAGER', 'ADMIN')")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<WeatherDataResponse> fetchForLocation(@PathVariable Long locationId) {
        return ResponseEntity.ok(weatherService.fetchAndStore(locationId));
    }

    @PostMapping("/fetch/all")
//    @PreAuthorize("hasRole('ADMIN')")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Integer> fetchForAll() {
        return ResponseEntity.ok(weatherService.fetchAndStoreForAll());
    }

    @GetMapping("/location/{locationId}/latest")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<WeatherDataResponse> getLatest(@PathVariable Long locationId) {
        return ResponseEntity.ok(weatherService.getLatest(locationId));
    }

    @GetMapping("/location/{locationId}/history")
//    @PreAuthorize("hasAnyRole('DISPATCHER', 'MANAGER', 'ADMIN')")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<WeatherDataResponse>> getHistory(
            @PathVariable Long locationId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ) {
        if (from.isAfter(to)) {
            throw new BadRequestException("'from' must be before 'to'");
        }
        if (from.plusDays(90).isBefore(to)) {
            throw new BadRequestException("History range cannot exceed 90 days");
        }
        return ResponseEntity.ok(weatherService.getHistory(locationId, from, to));
    }
}
