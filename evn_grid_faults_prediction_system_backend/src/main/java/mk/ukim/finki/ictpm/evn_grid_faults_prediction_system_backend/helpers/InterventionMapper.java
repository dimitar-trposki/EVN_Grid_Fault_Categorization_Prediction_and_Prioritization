package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.helpers;

import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.InterventionResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.InterventionSummaryResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.Intervention;
import org.springframework.stereotype.Component;

@Component
public class InterventionMapper {

    public InterventionResponse toResponse(Intervention i) {
        if (i == null) return null;
        return new InterventionResponse(
            i.getId(),
            i.getFaultReport().getId(),
            i.getCrew().getId(),
            i.getCrew().getName(),
            i.getLocation() != null ? i.getLocation().getId() : null,
            i.getStartedAt(),
            i.getEndedAt(),
            i.getDurationMinutes(),
            i.getResolutionStatus(),
            i.getResolutionNotes(),
            i.getRootCause()
        );
    }

    public InterventionSummaryResponse toSummaryResponse(Intervention i) {
        if (i == null) return null;
        return new InterventionSummaryResponse(
            i.getId(),
            i.getFaultReport().getId(),
            i.getCrew().getName(),
            i.getStartedAt(),
            i.getResolutionStatus()
        );
    }
}
