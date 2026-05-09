package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service;

import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.request.ManualPriorityOverrideRequest;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.FaultPriorityResponse;

import java.util.List;

public interface FaultPriorityService {

    FaultPriorityResponse calculatePriority(Long faultReportId);

    FaultPriorityResponse recalculate(Long faultReportId);

    FaultPriorityResponse getByFault(Long faultReportId);

    FaultPriorityResponse manualOverride(Long faultReportId, ManualPriorityOverrideRequest req);

    List<FaultPriorityResponse> getTopPriorityFaults(int limit);
}
