package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service;

import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.AddCrewMemberRequest;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.CreateCrewRequest;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.CrewMemberResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.CrewResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.CrewResponseDto;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.CrewSummaryResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.UpdateCrewRequest;

import java.util.List;

public interface CrewService {

    /** @deprecated Use {@link #listAll()} */
    @Deprecated
    List<CrewResponseDto> getAll();

    CrewResponse getById(Long id);

    /** @deprecated Use FaultAssignmentService.assignCrew instead */
    @Deprecated
    CrewResponseDto assignToFault(Long faultId, Long crewId);

    List<CrewSummaryResponse> listAll();

    CrewResponse create(CreateCrewRequest request);

    CrewResponse update(Long id, UpdateCrewRequest request);

    void delete(Long id);

    CrewResponse addMember(Long crewId, AddCrewMemberRequest request);

    void removeMember(Long crewId, Long memberId);

    List<CrewResponse> getAvailable();

    List<CrewMemberResponse> getMembers(Long crewId);

    CrewResponse updateLocation(Long crewId, Double latitude, Double longitude);
}
