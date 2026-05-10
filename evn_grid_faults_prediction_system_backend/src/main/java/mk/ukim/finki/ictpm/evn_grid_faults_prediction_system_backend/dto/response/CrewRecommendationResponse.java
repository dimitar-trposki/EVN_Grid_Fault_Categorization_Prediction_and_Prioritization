package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response;

public record CrewRecommendationResponse(
    Long crewId,
    String crewCode,
    String crewName,
    double score,
    String reason,
    double distanceKm
) {}
