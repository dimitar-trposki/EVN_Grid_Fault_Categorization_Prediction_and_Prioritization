package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.request;

import java.util.List;

public record ManualClassificationOverrideRequest(
        String predictedFaultCategory,
        String predictedSeverity,
        Boolean safetyRisk,
        List<String> extractedKeywords
) {}
