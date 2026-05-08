package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service;

import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.ImportBatchResponse;
import org.springframework.web.multipart.MultipartFile;

public interface ImportService {

    ImportBatchResponse importFaults(MultipartFile file, Long userId);

    ImportBatchResponse importLocations(MultipartFile file, Long userId);

    ImportBatchResponse importEquipment(MultipartFile file, Long userId);

    ImportBatchResponse getBatchStatus(Long batchId);
}
