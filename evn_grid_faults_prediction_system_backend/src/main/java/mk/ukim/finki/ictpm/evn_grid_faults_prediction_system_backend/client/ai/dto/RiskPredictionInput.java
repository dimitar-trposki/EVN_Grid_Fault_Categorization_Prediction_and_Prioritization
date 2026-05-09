package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.client.ai.dto;

public record RiskPredictionInput(
        Long locationId,
        Long equipmentId,
        Double latitude,
        Double longitude,
        Integer criticalityLevel,
        Integer recentFaultCount,
        Double equipmentAgeYears,
        Double weatherTemperature,
        Double weatherWindSpeed,
        Double weatherPrecipitation,
        String weatherCondition
) {}
