package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service;

import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.InterventionCreateDto;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.InterventionResponseDto;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.request.CloseFaultRequest;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.request.StartInterventionRequest;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.request.UpdateInterventionRequest;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.InterventionResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.InterventionSummaryResponse;

import java.util.List;

public interface InterventionService {

    /** @deprecated Use {@link #start(StartInterventionRequest, String)} */
    @Deprecated
    InterventionResponseDto create(Long faultId, InterventionCreateDto dto);

    /** @deprecated Use {@link #getById(Long)} */
    @Deprecated
    List<InterventionResponseDto> getByFault(Long faultId);

    InterventionResponse start(StartInterventionRequest request, String callerEmail);

    InterventionResponse update(Long id, UpdateInterventionRequest request, String callerEmail);

    InterventionResponse closeFault(Long faultReportId, CloseFaultRequest request, String callerEmail);

    InterventionResponse getById(Long id);

    InterventionResponse getByFaultReport(Long faultReportId);

    List<InterventionSummaryResponse> getByCrew(Long crewId);
}
