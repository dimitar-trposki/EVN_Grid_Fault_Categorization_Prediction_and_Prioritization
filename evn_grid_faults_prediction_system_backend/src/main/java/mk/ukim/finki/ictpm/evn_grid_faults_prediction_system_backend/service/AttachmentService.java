package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service;

import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.AttachmentDto;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.AttachmentResponse;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AttachmentService {

    /** @deprecated Use {@link #upload(Long, MultipartFile, String)} */
    @Deprecated
    AttachmentDto upload(Long faultId, MultipartFile file);

    /** @deprecated Use {@link #listAttachments(Long)} */
    @Deprecated
    List<AttachmentDto> getByFault(Long faultId);

    /** @deprecated Use {@link #delete(Long, Long)} */
    @Deprecated
    void delete(Long id);

    AttachmentResponse upload(Long faultId, MultipartFile file, String callerEmail);

    List<AttachmentResponse> listAttachments(Long faultId);

    Resource download(Long faultId, Long attachmentId, String callerEmail);

    void delete(Long faultId, Long attachmentId);
}
