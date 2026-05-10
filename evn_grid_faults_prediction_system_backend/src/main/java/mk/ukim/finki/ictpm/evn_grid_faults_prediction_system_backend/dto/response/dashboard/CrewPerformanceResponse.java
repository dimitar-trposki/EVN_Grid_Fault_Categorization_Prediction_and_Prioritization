package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.dashboard;

public record CrewPerformanceResponse(
        Long crewId,
        String crewName,
        String regionName,
        String status,
        long completedInterventions,
        double avgDurationMin,
        double efficiencyPercent
) {}
