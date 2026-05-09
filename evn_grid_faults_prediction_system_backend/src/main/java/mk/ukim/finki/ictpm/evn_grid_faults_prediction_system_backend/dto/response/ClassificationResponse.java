package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record ClassificationResponse(
        Long id,
        Long faultReportId,
        String predictedFaultCategory,
        String predictedSeverity,
        Double classificationConfidence,
        Boolean nlpProcessed,
        List<String> extractedKeywords,
        LocalDateTime classifiedAt,
        Boolean isFallback
) {}
