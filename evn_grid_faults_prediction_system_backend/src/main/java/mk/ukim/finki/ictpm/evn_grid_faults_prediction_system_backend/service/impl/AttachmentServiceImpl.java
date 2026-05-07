package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service.impl;

import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.AttachmentDto;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.Attachment;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.FaultReport;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.repository.AttachmentRepository;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.repository.FaultReportRepository;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service.AttachmentService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class AttachmentServiceImpl implements AttachmentService {

    private final AttachmentRepository attachmentRepo;
    private final FaultReportRepository faultRepo;

    public AttachmentServiceImpl(AttachmentRepository attachmentRepo,
                                 FaultReportRepository faultRepo) {
        this.attachmentRepo = attachmentRepo;
        this.faultRepo = faultRepo;
    }

    @Override
    public List<AttachmentDto> getByFault(Long faultId) {
        return attachmentRepo.findByFaultReportId(faultId)
                .stream()
                .map(a -> new AttachmentDto(a.getId(), a.getFileName()))
                .toList();
    }

    @Override
    public AttachmentDto upload(Long faultId, MultipartFile file) {
        FaultReport fault = faultRepo.findById(faultId)
                .orElseThrow(() -> new RuntimeException("Fault not found with id: " + faultId));

        Attachment attachment = new Attachment();
        attachment.setFaultReport(fault);
        attachment.setFileName(file.getOriginalFilename());

        attachment = attachmentRepo.save(attachment);

        return new AttachmentDto(attachment.getId(), attachment.getFileName());
    }

    @Override
    public void delete(Long id) {
        attachmentRepo.deleteById(id);
    }
}