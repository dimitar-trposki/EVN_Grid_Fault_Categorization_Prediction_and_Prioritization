package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto;

import java.util.List;

public record CrewResponse(
    Long id,
    String name,
    List<CrewMemberResponse> members
) {}
