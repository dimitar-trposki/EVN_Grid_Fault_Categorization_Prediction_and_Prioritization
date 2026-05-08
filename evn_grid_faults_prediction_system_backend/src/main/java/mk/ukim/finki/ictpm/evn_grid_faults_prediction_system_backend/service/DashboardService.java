package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service;

import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.dashboard.CrewPerformanceResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.dashboard.DashboardKpiResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.dashboard.FaultsByPeriodResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.dashboard.FaultsByRegionResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.dashboard.FaultsByTypeResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.dashboard.MapCrewLocationResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.dashboard.MapFaultResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.dashboard.MapRiskZoneResponse;

import java.time.LocalDateTime;
import java.util.List;

public interface DashboardService {

    DashboardKpiResponse getKpis();

    List<FaultsByRegionResponse> getFaultsByRegion();

    List<FaultsByTypeResponse> getFaultsByType();

    List<FaultsByPeriodResponse> getFaultsByPeriod(String groupBy, LocalDateTime from, LocalDateTime to);

    List<CrewPerformanceResponse> getCrewPerformance();

    List<MapFaultResponse> getActiveFaultsForMap();

    List<MapRiskZoneResponse> getRiskZonesForMap();

    List<MapCrewLocationResponse> getCrewsForMap();
}
