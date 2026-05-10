package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response;

import java.time.LocalDateTime;

public record InterventionSummaryResponse(
    Long id,
    Long faultReportId,
    String crewName,
    LocalDateTime startedAt,
    String resolutionStatus
) {}
