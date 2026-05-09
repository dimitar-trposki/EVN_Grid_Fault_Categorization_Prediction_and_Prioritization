package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.client.ai.dto;

import java.util.List;

public record RiskPredictionResult(
        Double riskScore,
        String riskLevel,
        List<String> contributingFactors,
        Boolean isFallback
) {
    public static RiskPredictionResult fallback() {
        return new RiskPredictionResult(50.0, "MEDIUM", List.of("AI service unavailable"), true);
    }
}
