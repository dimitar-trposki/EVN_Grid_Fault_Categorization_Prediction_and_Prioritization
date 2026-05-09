package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response;

import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.enums.WeatherCondition;

import java.time.LocalDateTime;

public record WeatherDataResponse(
        Long id,
        Long locationId,
        LocalDateTime recordedAt,
        Double temperature,
        Double windSpeed,
        Double humidity,
        Double precipitation,
        WeatherCondition weatherCondition,
        String sourceApi
) {}
