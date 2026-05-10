package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response;

import java.time.LocalDateTime;

public record InterventionResponse(
    Long id,
    Long faultReportId,
    Long crewId,
    String crewName,
    Long locationId,
    LocalDateTime startedAt,
    LocalDateTime endedAt,
    Integer durationMinutes,
    String resolutionStatus,
    String resolutionNotes,
    String rootCause
) {}
