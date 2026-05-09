package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service.impl;

import lombok.RequiredArgsConstructor;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.dashboard.CrewPerformanceResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.dashboard.DashboardKpiResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.dashboard.FaultsByPeriodResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.dashboard.FaultsByRegionResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.dashboard.FaultsByTypeResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.dashboard.MapCrewLocationResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.dashboard.MapFaultResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.dashboard.MapRiskZoneResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.exception.BadRequestException;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.Crew;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.Location;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.RiskPrediction;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.enums.FaultStatus;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.enums.FaultType;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.repository.CrewRepository;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.repository.FaultAssigmentRepository;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.repository.FaultReportRepository;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.repository.InterventionRepository;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.repository.RiskPredictionRepository;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service.DashboardService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private static final List<FaultStatus> ACTIVE_ASSIGNMENT_STATUSES =
            List.of(FaultStatus.ASSIGNED, FaultStatus.IN_PROGRESS);

    private final FaultReportRepository faultReportRepository;
    private final InterventionRepository interventionRepository;
    private final RiskPredictionRepository riskPredictionRepository;
    private final CrewRepository crewRepository;
    private final FaultAssigmentRepository faultAssigmentRepository;

    @Override
    public DashboardKpiResponse getKpis() {
        long totalActiveFaults = faultReportRepository.countActiveFaults();
        long criticalFaults = faultReportRepository.countCriticalActiveFaults();
        double avgResponseTime = Optional.ofNullable(faultReportRepository.avgResponseTimeMinutes()).orElse(0.0);
        double avgResolutionTime = Optional.ofNullable(faultReportRepository.avgResolutionTimeMinutes()).orElse(0.0);
        long crewsTotal = crewRepository.count();
        long crewsActive = faultAssigmentRepository.countDistinctActiveCrews(ACTIVE_ASSIGNMENT_STATUSES);
        long faultsToday = faultReportRepository.countFaultsToday();

        return new DashboardKpiResponse(
                totalActiveFaults,
                criticalFaults,
                avgResponseTime,
                avgResolutionTime,
                crewsActive,
                crewsTotal,
                faultsToday
        );
    }

    @Override
    public List<FaultsByRegionResponse> getFaultsByRegion() {
        return faultReportRepository.countByRegion().stream()
                .map(row -> new FaultsByRegionResponse(
                        ((Number) row[0]).longValue(),
                        (String) row[1],
                        ((Number) row[2]).longValue()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public List<FaultsByTypeResponse> getFaultsByType() {
        return faultReportRepository.countByFaultType().stream()
                .map(row -> new FaultsByTypeResponse(
                        ((FaultType) row[0]).name(),
                        ((Number) row[1]).longValue()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public List<FaultsByPeriodResponse> getFaultsByPeriod(String groupBy, LocalDateTime from, LocalDateTime to) {
        LocalDateTime effectiveFrom = Optional.ofNullable(from).orElse(LocalDateTime.now().minusMonths(1));
        LocalDateTime effectiveTo = Optional.ofNullable(to).orElse(LocalDateTime.now());

        if (effectiveFrom.isAfter(effectiveTo)) {
            throw new BadRequestException("'from' must be before 'to'");
        }

        List<Object[]> raw = switch (groupBy.toLowerCase()) {
            case "day" -> faultReportRepository.countByPeriodDay(effectiveFrom, effectiveTo);
            case "week" -> faultReportRepository.countByPeriodWeek(effectiveFrom, effectiveTo);
            case "month" -> faultReportRepository.countByPeriodMonth(effectiveFrom, effectiveTo);
            default -> throw new BadRequestException("groupBy must be 'day', 'week', or 'month'");
        };

        return raw.stream()
                .map(row -> new FaultsByPeriodResponse(
                        (String) row[0],
                        ((Number) row[1]).longValue()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public List<CrewPerformanceResponse> getCrewPerformance() {
        // avgDurationMin and efficiencyPercent cannot be computed — Intervention has no timestamp fields.
        return interventionRepository.countByCrew().stream()
                .map(row -> new CrewPerformanceResponse(
                        ((Number) row[0]).longValue(),
                        (String) row[1],
                        ((Number) row[2]).longValue(),
                        0.0,
                        0.0
                ))
                .collect(Collectors.toList());
    }

    @Override
    public List<MapFaultResponse> getActiveFaultsForMap() {
        return faultReportRepository.findActiveFaultsForMap().stream()
                .map(row -> new MapFaultResponse(
                        ((Number) row[0]).longValue(),
                        ((Number) row[1]).doubleValue(),
                        ((Number) row[2]).doubleValue(),
                        (String) row[3],
                        (String) row[4],
                        (String) row[5],
                        toLocalDateTime(row[6])
                ))
                .collect(Collectors.toList());
    }

    @Override
    public List<MapRiskZoneResponse> getRiskZonesForMap() {
        return riskPredictionRepository.findTopRiskZonesWithLocation().stream()
                .limit(50)
                .map(this::toMapRiskZoneResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<MapCrewLocationResponse> getCrewsForMap() {
        List<Crew> allCrews = crewRepository.findAll();
        Set<Long> activeCrewIds = new HashSet<>(
                faultAssigmentRepository.findActiveCrewIds(ACTIVE_ASSIGNMENT_STATUSES)
        );

        return allCrews.stream()
                .map(crew -> new MapCrewLocationResponse(
                        crew.getId(),
                        crew.getName(),
                        activeCrewIds.contains(crew.getId()) ? "ACTIVE" : "IDLE",
                        null, // Crew entity has no latitude field
                        null  // Crew entity has no longitude field
                ))
                .collect(Collectors.toList());
    }

    private MapRiskZoneResponse toMapRiskZoneResponse(RiskPrediction rp) {
        Location location = rp.getLocation();
        double score = rp.getRiskScore() != null ? rp.getRiskScore() : 0.0;
        String level = rp.getRiskLevel() != null ? rp.getRiskLevel()
                : (score > 70 ? "HIGH" : score > 40 ? "MEDIUM" : "LOW");
        return new MapRiskZoneResponse(
                location.getId(),
                location.getLatitude(),
                location.getLongitude(),
                score,
                level
        );
    }

    private LocalDateTime toLocalDateTime(Object val) {
        if (val == null) return null;
        if (val instanceof Timestamp ts) return ts.toLocalDateTime();
        if (val instanceof LocalDateTime ldt) return ldt;
        return null;
    }
}
