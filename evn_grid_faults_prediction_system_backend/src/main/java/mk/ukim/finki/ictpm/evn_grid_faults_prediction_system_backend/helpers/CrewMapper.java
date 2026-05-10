package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.helpers;

import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.CrewMemberResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.CrewResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.CrewSummaryResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.Crew;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CrewMapper {

    private final CrewMemberMapper crewMemberMapper;

    public CrewMapper(CrewMemberMapper crewMemberMapper) {
        this.crewMemberMapper = crewMemberMapper;
    }

    public CrewResponse toResponse(Crew crew) {
        if (crew == null) {
            return null;
        }
        List<CrewMemberResponse> memberResponses = new ArrayList<>();
        if (crew.getCrewMembers() != null) {
            memberResponses = crew.getCrewMembers().stream()
                .map(crewMemberMapper::toResponse)
                .collect(Collectors.toList());
        }
        int memberCount = memberResponses.size();
        return new CrewResponse(
            crew.getId(),
            crew.getName(),
            crew.getCrewCode(),
            crew.getStatus(),
            crew.getCurrentLatitude(),
            crew.getCurrentLongitude(),
            memberCount,
            memberResponses
        );
    }

    public CrewSummaryResponse toSummaryResponse(Crew crew) {
        if (crew == null) {
            return null;
        }
        int count = crew.getCrewMembers() != null ? crew.getCrewMembers().size() : 0;
        return new CrewSummaryResponse(
            crew.getId(),
            crew.getName(),
            crew.getCrewCode(),
            crew.getStatus(),
            count
        );
    }
}
