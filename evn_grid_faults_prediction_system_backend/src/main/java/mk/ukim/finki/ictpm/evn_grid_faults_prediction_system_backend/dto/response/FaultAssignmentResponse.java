package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response;

import java.time.LocalDateTime;

public record FaultAssignmentResponse(
    Long id,
    Long faultReportId,
    Long crewId,
    String crewName,
    Long assignedByUserId,
    String assignedByUserName,
    LocalDateTime assignedAt,
    LocalDateTime acceptedAt,
    LocalDateTime completedAt,
    String assignmentStatus,
    String assignmentNote
) {}
