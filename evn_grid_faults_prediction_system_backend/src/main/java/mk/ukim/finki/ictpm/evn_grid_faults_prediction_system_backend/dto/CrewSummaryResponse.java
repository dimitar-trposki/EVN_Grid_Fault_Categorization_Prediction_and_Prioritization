package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto;

import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.enums.CrewStatus;

public record CrewSummaryResponse(
    Long id,
    String name,
    String crewCode,
    CrewStatus status,
    int memberCount,
    Double latitude,
    Double longitude,
    String regionName
) {}
