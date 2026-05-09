package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service;

import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.request.ManualClassificationOverrideRequest;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.ClassificationResponse;

public interface FaultClassificationService {

    ClassificationResponse classifyFault(Long faultReportId);

    ClassificationResponse reclassify(Long faultReportId);

    ClassificationResponse getByFault(Long faultReportId);

    ClassificationResponse manualOverride(Long faultReportId, ManualClassificationOverrideRequest req);
}
