package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.helpers;

import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.RiskPredictionResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.RiskPrediction;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RiskPredictionMapper {

    public RiskPredictionResponse toResponse(RiskPrediction entity) {
        if (entity == null) return null;
        return new RiskPredictionResponse(
                entity.getId(),
                entity.getLocation() != null ? entity.getLocation().getId() : null,
                entity.getEquipment() != null ? entity.getEquipment().getId() : null,
                entity.getRiskScore(),
                entity.getRiskLevel(),
                deserializeFactors(entity.getContributingFactors()),
                entity.getPredictionDate(),
                entity.getIsFallback()
        );
    }

    public String serializeFactors(List<String> factors) {
        if (factors == null || factors.isEmpty()) return "";
        return String.join(" | ", factors);
    }

    public List<String> deserializeFactors(String factors) {
        if (factors == null || factors.isBlank()) return List.of();
        return List.of(factors.split(" \\| "));
    }
}
