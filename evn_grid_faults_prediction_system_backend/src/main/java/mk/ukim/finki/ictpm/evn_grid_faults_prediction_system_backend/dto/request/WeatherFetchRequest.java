package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.request;

import jakarta.validation.constraints.NotNull;

public record WeatherFetchRequest(
        @NotNull(message = "Location ID cannot be null")
        Long locationId
) {
}
