package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.helpers;

import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.FaultAssignmentResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.FaultAssignment;
import org.springframework.stereotype.Component;

@Component
public class FaultAssignmentMapper {

    public FaultAssignmentResponse toResponse(FaultAssignment fa) {
        if (fa == null) return null;
        String assignedByName = null;
        Long assignedById = null;
        if (fa.getAssignedByUser() != null) {
            assignedById = fa.getAssignedByUser().getId();
            assignedByName = fa.getAssignedByUser().getFirstName() + " " + fa.getAssignedByUser().getLastName();
        }
        return new FaultAssignmentResponse(
            fa.getId(),
            fa.getFaultReport().getId(),
            fa.getCrew().getId(),
            fa.getCrew().getName(),
            assignedById,
            assignedByName,
            fa.getAssignedAt(),
            fa.getAcceptedAt(),
            fa.getCompletedAt(),
            fa.getAssignmentStatus(),
            fa.getAssignmentNote()
        );
    }
}
