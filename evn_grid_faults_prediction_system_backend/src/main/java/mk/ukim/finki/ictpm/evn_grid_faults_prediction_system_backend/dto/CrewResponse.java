package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto;

import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.enums.CrewStatus;

import java.util.List;

public record CrewResponse(
    Long id,
    String name,
    String crewCode,
    CrewStatus status,
    Double currentLatitude,
    Double currentLongitude,
    int memberCount,
    List<CrewMemberResponse> members
) {}
