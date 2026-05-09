package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.request;

import jakarta.validation.constraints.NotNull;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.enums.FaultStatus;

public record FaultStatusUpdateRequest(
        @NotNull FaultStatus status,
        String note,
        Boolean customerVisible
) {}
