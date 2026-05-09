package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response;

import java.time.LocalDateTime;

public record WeatherDataResponse(
        Double temperature,
        Double windSpeed,
        Double humidity,
        Double precipitation,
        String condition,
        LocalDateTime recordedAt
) {
}
