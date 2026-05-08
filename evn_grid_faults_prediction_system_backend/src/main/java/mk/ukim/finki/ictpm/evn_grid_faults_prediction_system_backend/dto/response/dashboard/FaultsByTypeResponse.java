package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.dashboard;

public record FaultsByTypeResponse(
        String faultTypeName,
        long count
) {}
