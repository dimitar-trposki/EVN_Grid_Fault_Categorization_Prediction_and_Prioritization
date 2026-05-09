package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.client.ai.dto;

import java.util.List;

public record ClassificationResult(
        String predictedCategory,
        String predictedSeverity,
        Double confidence,
        List<String> keywords,
        Boolean safetyRisk,
        Boolean isFallback
) {
    public static ClassificationResult fallback() {
        return new ClassificationResult("UNKNOWN", "MEDIUM", 0.0, List.of(), false, true);
    }
}
