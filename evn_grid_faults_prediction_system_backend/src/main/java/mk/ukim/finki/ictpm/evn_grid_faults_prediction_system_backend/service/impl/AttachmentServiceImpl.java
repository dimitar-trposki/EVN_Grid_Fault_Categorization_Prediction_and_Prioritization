package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.AttachmentDto;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.AttachmentResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.exception.BadRequestException;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.exception.ResourceNotFoundException;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.helpers.AttachmentMapper;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.Attachment;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.Customer;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.FaultReport;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.User;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.enums.RoleType;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.repository.AttachmentRepository;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.repository.FaultReportRepository;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.repository.UserRepository;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service.AttachmentService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AttachmentServiceImpl implements AttachmentService {

    private final AttachmentRepository attachmentRepo;
    private final FaultReportRepository faultRepo;
    private final UserRepository userRepository;
    private final AttachmentMapper attachmentMapper;

    @Value("${app.attachments.storage-path:./uploads/attachments}")
    private String storagePath;

    @Value("${app.attachments.max-size-bytes:10485760}")
    private long maxSizeBytes;

    @Value("${app.attachments.allowed-types:image/jpeg,image/png,image/jpg,application/pdf}")
    private String allowedTypes;

    // --- Deprecated legacy methods ---

    @Override
    @Deprecated
    public List<AttachmentDto> getByFault(Long faultId) {
        return attachmentRepo.findByFaultReportId(faultId)
                .stream()
                .map(a -> new AttachmentDto(a.getId(), a.getFileName()))
                .toList();
    }

    @Override
    @Deprecated
    public AttachmentDto upload(Long faultId, MultipartFile file) {
        FaultReport fault = faultRepo.findById(faultId)
                .orElseThrow(() -> new ResourceNotFoundException("FaultReport", faultId));
        Attachment attachment = new Attachment();
        attachment.setFaultReport(fault);
        attachment.setFileName(file.getOriginalFilename() != null ? file.getOriginalFilename() : "file");
        attachment.setFilePath("legacy-upload");
        attachment.setFileType(file.getContentType() != null ? file.getContentType() : "application/octet-stream");
        attachment.setFileSize(file.getSize());
        attachment.setUploadedAt(LocalDateTime.now());
        attachment = attachmentRepo.save(attachment);
        return new AttachmentDto(attachment.getId(), attachment.getFileName());
    }

    @Override
    @Deprecated
    public void delete(Long id) {
        attachmentRepo.deleteById(id);
    }

    // --- New methods ---

    @Override
    public List<AttachmentResponse> listAttachments(Long faultId) {
        if (!faultRepo.existsById(faultId)) {
            throw new ResourceNotFoundException("FaultReport", faultId);
        }
        return attachmentRepo.findByFaultReportId(faultId)
                .stream()
                .map(attachmentMapper::toResponse)
                .toList();
    }

    @Override
    public AttachmentResponse upload(Long faultId, MultipartFile file, String callerEmail) {
        FaultReport fault = faultRepo.findById(faultId)
                .orElseThrow(() -> new ResourceNotFoundException("FaultReport", faultId));

        String contentType = file.getContentType() != null ? file.getContentType() : "application/octet-stream";
        List<String> allowed = Arrays.asList(allowedTypes.split(","));
        if (!allowed.contains(contentType)) {
            throw new BadRequestException("File type not allowed: " + contentType + ". Allowed: " + allowedTypes);
        }
        if (file.getSize() > maxSizeBytes) {
            throw new BadRequestException("File size " + file.getSize() + " exceeds limit of " + maxSizeBytes + " bytes");
        }

        User user = userRepository.findByEmail(callerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User for email: " + callerEmail));
        User uploadedByUser = null;
        Customer uploadedByCustomer = null;
        if (user.getUserRole() == RoleType.CUSTOMER) {
            uploadedByCustomer = user.getCustomer();
        } else {
            uploadedByUser = user;
        }

        String originalName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "file";
        String extension = originalName.contains(".")
                ? originalName.substring(originalName.lastIndexOf("."))
                : "";
        String storedName = UUID.randomUUID() + extension;
        Path dir = Paths.get(storagePath);
        try {
            Files.createDirectories(dir);
            Files.copy(file.getInputStream(), dir.resolve(storedName), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Failed to store attachment: " + e.getMessage(), e);
        }

        Attachment attachment = new Attachment();
        attachment.setFaultReport(fault);
        attachment.setFileName(originalName);
        attachment.setFilePath(dir.resolve(storedName).toString());
        attachment.setFileType(contentType);
        attachment.setFileSize(file.getSize());
        attachment.setUploadedAt(LocalDateTime.now());
        attachment.setUploadedByUser(uploadedByUser);
        attachment.setUploadedByCustomer(uploadedByCustomer);

        return attachmentMapper.toResponse(attachmentRepo.save(attachment));
    }

    @Override
    public Resource download(Long faultId, Long attachmentId, String callerEmail) {
        if (!faultRepo.existsById(faultId)) {
            throw new ResourceNotFoundException("FaultReport", faultId);
        }
        if (!attachmentRepo.existsByIdAndFaultReportId(attachmentId, faultId)) {
            throw new ResourceNotFoundException("Attachment", attachmentId);
        }
        Attachment attachment = attachmentRepo.findById(attachmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment", attachmentId));
        Path filePath = Paths.get(attachment.getFilePath());
        Resource resource = new FileSystemResource(filePath);
        if (!resource.exists() || !resource.isReadable()) {
            throw new ResourceNotFoundException("File not found on disk for attachment " + attachmentId);
        }
        return resource;
    }

    @Override
    public void delete(Long faultId, Long attachmentId) {
        if (!faultRepo.existsById(faultId)) {
            throw new ResourceNotFoundException("FaultReport", faultId);
        }
        if (!attachmentRepo.existsByIdAndFaultReportId(attachmentId, faultId)) {
            throw new ResourceNotFoundException("Attachment", attachmentId);
        }
        Attachment attachment = attachmentRepo.findById(attachmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment", attachmentId));
        try {
            Files.deleteIfExists(Paths.get(attachment.getFilePath()));
        } catch (IOException e) {
            log.warn("Could not delete physical file for attachment id={}: {}", attachmentId, e.getMessage());
        }
        attachmentRepo.deleteById(attachmentId);
    }
}
