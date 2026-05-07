package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateRegionRequest(
        @NotBlank
        @Size(max = 80)
        String name
) {}