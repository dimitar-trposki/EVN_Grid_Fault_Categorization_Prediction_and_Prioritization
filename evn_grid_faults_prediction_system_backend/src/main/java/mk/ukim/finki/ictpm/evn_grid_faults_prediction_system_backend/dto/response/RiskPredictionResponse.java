package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record RiskPredictionResponse(
        Long id,
        Long locationId,
        Long equipmentId,
        Double riskScore,
        String riskLevel,
        List<String> contributingFactors,
        LocalDateTime predictionDate,
        Boolean isFallback
) {}
