//package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.web;
//
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.request.WeatherFetchRequest;
//import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.WeatherDataResponse;
//import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service.WeatherService;
//import org.springframework.format.annotation.DateTimeFormat;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.web.bind.annotation.*;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/v1")
//@RequiredArgsConstructor
//public class WeatherController {
//
//    private final WeatherService weatherService;
//
//    @PostMapping("/weather/fetch")
//    @PreAuthorize("hasAnyRole('ADMIN', 'DISPATCHER')")
//    public ResponseEntity<WeatherDataResponse> fetchWeather(@Valid @RequestBody WeatherFetchRequest request) {
//        WeatherDataResponse response = weatherService.fetchAndStore(request.locationId());
//        return ResponseEntity.ok(response);
//    }
//
//    @GetMapping("/locations/{id}/weather")
//    public ResponseEntity<WeatherDataResponse> getLatestWeather(@PathVariable("id") Long locationId) {
//        WeatherDataResponse response = weatherService.getLatest(locationId);
//        return ResponseEntity.ok(response);
//    }
//
//    @GetMapping("/locations/{id}/weather/history")
//    public ResponseEntity<List<WeatherDataResponse>> getWeatherHistory(
//            @PathVariable("id") Long locationId,
//            @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
//            @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to) {
//        List<WeatherDataResponse> response = weatherService.getHistory(locationId, from, to);
//        return ResponseEntity.ok(response);
//    }
//}
