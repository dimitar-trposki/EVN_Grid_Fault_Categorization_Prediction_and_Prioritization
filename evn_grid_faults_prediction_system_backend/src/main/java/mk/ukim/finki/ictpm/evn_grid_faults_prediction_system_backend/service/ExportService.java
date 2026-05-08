package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service;

import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.request.ExportRequest;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.ExportBatchResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.User;
import org.springframework.core.io.Resource;

public interface ExportService {

    ExportBatchResponse exportFaults(ExportRequest request, Long userId);

    ExportBatchResponse exportInterventions(ExportRequest request, Long userId);

    ExportBatchResponse exportAnalytics(ExportRequest request, Long userId);

    Resource getFile(Long batchId, User requestingUser);
}
