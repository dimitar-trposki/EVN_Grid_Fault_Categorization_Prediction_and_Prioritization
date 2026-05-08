package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.dashboard;

public record DashboardKpiResponse(
        long totalActiveFaults,
        long criticalFaults,
        double avgResponseTimeMin,
        double avgResolutionTimeMin,
        long crewsActive,
        long crewsTotal,
        long faultsToday
) {}
