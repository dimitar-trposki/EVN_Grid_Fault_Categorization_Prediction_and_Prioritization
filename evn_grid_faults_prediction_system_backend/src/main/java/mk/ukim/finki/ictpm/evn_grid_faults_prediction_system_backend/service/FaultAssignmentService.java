package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service;

import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.request.AssignCrewRequest;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.request.ReassignCrewRequest;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.CrewRecommendationResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.FaultAssignmentResponse;

import java.util.List;

public interface FaultAssignmentService {

    FaultAssignmentResponse assignCrew(AssignCrewRequest request, String callerEmail);

    FaultAssignmentResponse reassignCrew(Long faultReportId, ReassignCrewRequest request, String callerEmail);

    FaultAssignmentResponse acceptAssignment(Long assignmentId, String callerEmail);

    FaultAssignmentResponse getByFault(Long faultReportId);

    List<FaultAssignmentResponse> getByCrew(Long crewId);

    List<CrewRecommendationResponse> recommendCrews(Long faultReportId);
}
