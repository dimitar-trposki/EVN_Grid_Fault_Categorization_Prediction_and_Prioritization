package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.request;

import jakarta.validation.constraints.NotNull;

public record ReassignCrewRequest(
    @NotNull(message = "New crew ID is required")
    Long newCrewId,

    String note
) {}
