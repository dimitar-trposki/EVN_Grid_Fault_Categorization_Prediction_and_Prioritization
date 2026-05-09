package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service;

import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.CrewMemberResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.AddCrewMemberRequest;

import java.util.List;

public interface CrewMemberService {

    List<CrewMemberResponse> findByCrewId(Long crewId);

    CrewMemberResponse findByUserId(Long userId);

    CrewMemberResponse addMemberToCrew(Long crewId, AddCrewMemberRequest request);

    void removeMemberFromCrew(Long crewId, Long memberId);

}
