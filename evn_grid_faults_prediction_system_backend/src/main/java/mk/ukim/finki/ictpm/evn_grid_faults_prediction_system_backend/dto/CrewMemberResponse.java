package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto;

public record CrewMemberResponse(
    Long id,
    String firstName,
    String lastName,
    Long userId,
    String position
) {}
