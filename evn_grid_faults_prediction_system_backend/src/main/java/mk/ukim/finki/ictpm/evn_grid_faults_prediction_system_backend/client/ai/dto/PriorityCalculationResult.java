package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.client.ai.dto;

public record PriorityCalculationResult(
        Double priorityScore,
        String priorityLevel,
        String explanation,
        Boolean isFallback
) {
    public static PriorityCalculationResult fallback() {
        return new PriorityCalculationResult(
                50.0, "MEDIUM",
                "AI service unavailable, default priority assigned",
                true);
    }
}
