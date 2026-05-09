package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.enums.FaultType;

public record OperatorCreateFaultRequest(
        @NotBlank @Size(max = 80) String title,
        @NotBlank String description,
        @NotNull Long locationId,
        @NotNull FaultType faultType,
        Long customerId
) {}
