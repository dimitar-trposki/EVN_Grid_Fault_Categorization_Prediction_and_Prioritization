package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.helpers;

import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.CrewMemberResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.CrewMember;
import org.springframework.stereotype.Component;

@Component
public class CrewMemberMapper {

    public CrewMemberResponse toResponse(CrewMember member) {
        if (member == null) {
            return null;
        }
        return new CrewMemberResponse(
            member.getId(),
            member.getFirstName(),
            member.getLastName(),
            member.getUser() != null ? member.getUser().getId() : null,
            member.getPosition(),
            member.getUser() != null ? member.getUser().getEmail() : null,
            member.getAssignedAt()
        );
    }
}
