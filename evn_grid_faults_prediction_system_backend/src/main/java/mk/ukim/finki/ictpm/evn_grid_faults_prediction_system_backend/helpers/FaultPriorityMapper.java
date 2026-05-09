package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.helpers;

import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.FaultPriorityResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.FaultPriorityRecord;
import org.springframework.stereotype.Component;

@Component
public class FaultPriorityMapper {

    public FaultPriorityResponse toResponse(FaultPriorityRecord entity) {
        if (entity == null) return null;
        return new FaultPriorityResponse(
                entity.getId(),
                entity.getFaultReport().getId(),
                entity.getPriorityLevel(),
                entity.getPriorityScore(),
                entity.getExplanation(),
                entity.getCalculatedAt(),
                entity.getCalculationSource(),
                entity.getIsFallback()
        );
    }
}
