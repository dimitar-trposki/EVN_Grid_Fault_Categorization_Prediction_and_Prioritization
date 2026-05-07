package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateLocationRequest(
        @NotNull
        Double latitude,

        @NotNull
        Double longitude,

        @NotBlank
        @Size(max = 100)
        String address,

        @NotNull
        Long regionId
) {
}
