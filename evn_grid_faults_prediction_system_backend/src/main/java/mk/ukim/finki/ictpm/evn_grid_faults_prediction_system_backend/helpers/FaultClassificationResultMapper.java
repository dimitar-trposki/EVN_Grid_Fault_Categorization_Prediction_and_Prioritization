package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.helpers;

import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.ClassificationResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.FaultClassificationResult;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class FaultClassificationResultMapper {

    public ClassificationResponse toResponse(FaultClassificationResult entity) {
        if (entity == null) return null;
        return new ClassificationResponse(
                entity.getId(),
                entity.getFaultReport().getId(),
                entity.getPredictedFaultCategory(),
                entity.getPredictedSeverity(),
                entity.getClassificationConfidence(),
                entity.getNlpProcessed(),
                entity.getExtractedKeywords() != null ? entity.getExtractedKeywords() : List.of(),
                entity.getClassifiedAt(),
                entity.getIsFallback()
        );
    }
}
