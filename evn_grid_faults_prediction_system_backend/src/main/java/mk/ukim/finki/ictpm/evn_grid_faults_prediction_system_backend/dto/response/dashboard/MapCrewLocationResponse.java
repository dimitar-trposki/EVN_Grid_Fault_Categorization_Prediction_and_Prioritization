package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.dashboard;

public record MapCrewLocationResponse(
        Long crewId,
        String name,
        String status,
        Double latitude,
        Double longitude
) {}
