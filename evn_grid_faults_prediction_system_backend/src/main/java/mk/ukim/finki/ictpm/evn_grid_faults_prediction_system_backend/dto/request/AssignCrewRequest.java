package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.request;

import jakarta.validation.constraints.NotNull;

public record AssignCrewRequest(
    @NotNull(message = "Fault report ID is required")
    Long faultReportId,

    @NotNull(message = "Crew ID is required")
    Long crewId,

    String note
) {}
