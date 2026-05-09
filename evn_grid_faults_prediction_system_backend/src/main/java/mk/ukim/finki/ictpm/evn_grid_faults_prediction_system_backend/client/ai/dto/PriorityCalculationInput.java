package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.client.ai.dto;

public record PriorityCalculationInput(
        Long faultId,
        String faultCategory,
        String severity,
        Boolean safetyRisk,
        Integer affectedUsersEstimate,
        String locationCriticality,
        String weatherCondition,
        Boolean isRecurring
) {}
