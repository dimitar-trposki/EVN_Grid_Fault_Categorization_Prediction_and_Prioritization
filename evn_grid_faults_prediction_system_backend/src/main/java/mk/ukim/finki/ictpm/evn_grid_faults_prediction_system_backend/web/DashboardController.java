package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.web;

import lombok.RequiredArgsConstructor;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.dashboard.CrewPerformanceResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.dashboard.DashboardKpiResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.dashboard.FaultsByPeriodResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.dashboard.FaultsByRegionResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.dashboard.FaultsByTypeResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.dashboard.MapCrewLocationResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.dashboard.MapFaultResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.dashboard.MapRiskZoneResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service.DashboardService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/kpis")
//    @PreAuthorize("hasAnyRole('DISPATCHER', 'MANAGER', 'ADMIN', 'OPERATOR')")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<DashboardKpiResponse> getKpis() {
        return ResponseEntity.ok(dashboardService.getKpis());
    }

    @GetMapping("/faults-by-region")
//    @PreAuthorize("hasAnyRole('DISPATCHER', 'MANAGER', 'ADMIN', 'OPERATOR')")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<FaultsByRegionResponse>> getFaultsByRegion() {
        return ResponseEntity.ok(dashboardService.getFaultsByRegion());
    }

    @GetMapping("/faults-by-type")
//    @PreAuthorize("hasAnyRole('DISPATCHER', 'MANAGER', 'ADMIN', 'OPERATOR')")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<FaultsByTypeResponse>> getFaultsByType() {
        return ResponseEntity.ok(dashboardService.getFaultsByType());
    }

    @GetMapping("/faults-by-period")
//    @PreAuthorize("hasAnyRole('DISPATCHER', 'MANAGER', 'ADMIN', 'OPERATOR')")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<FaultsByPeriodResponse>> getFaultsByPeriod(
            @RequestParam(defaultValue = "day") String groupBy,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ) {
        return ResponseEntity.ok(dashboardService.getFaultsByPeriod(groupBy, from, to));
    }

    @GetMapping("/crew-performance")
//    @PreAuthorize("hasAnyRole('DISPATCHER', 'MANAGER', 'ADMIN')")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<CrewPerformanceResponse>> getCrewPerformance() {
        return ResponseEntity.ok(dashboardService.getCrewPerformance());
    }

    @GetMapping("/map/faults")
//    @PreAuthorize("hasAnyRole('DISPATCHER', 'MANAGER', 'ADMIN', 'OPERATOR', 'FIELD_CREW')")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<MapFaultResponse>> getActiveFaultsForMap() {
        return ResponseEntity.ok(dashboardService.getActiveFaultsForMap());
    }

    @GetMapping("/map/risk-zones")
//    @PreAuthorize("hasAnyRole('DISPATCHER', 'MANAGER', 'ADMIN', 'OPERATOR')")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<MapRiskZoneResponse>> getRiskZonesForMap() {
        return ResponseEntity.ok(dashboardService.getRiskZonesForMap());
    }

    @GetMapping("/map/crews")
//    @PreAuthorize("hasAnyRole('DISPATCHER', 'MANAGER', 'ADMIN')")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<MapCrewLocationResponse>> getCrewsForMap() {
        return ResponseEntity.ok(dashboardService.getCrewsForMap());
    }
}
