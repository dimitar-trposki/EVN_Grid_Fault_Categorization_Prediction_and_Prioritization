package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service;

import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.AttachmentDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AttachmentService {

    AttachmentDto upload(Long faultId, MultipartFile file);

    List<AttachmentDto> getByFault(Long faultId);

    void delete(Long id);
}