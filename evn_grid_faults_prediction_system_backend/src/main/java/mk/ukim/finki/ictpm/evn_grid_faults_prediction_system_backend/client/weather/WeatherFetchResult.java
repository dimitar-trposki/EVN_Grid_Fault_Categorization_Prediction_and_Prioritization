package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.client.weather;

import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.enums.WeatherCondition;

import java.time.LocalDateTime;

public record WeatherFetchResult(
        Double temperature,
        Double windSpeed,
        Double humidity,
        Double precipitation,
        WeatherCondition weatherCondition,
        String sourceApi,
        LocalDateTime recordedAt
) {}
