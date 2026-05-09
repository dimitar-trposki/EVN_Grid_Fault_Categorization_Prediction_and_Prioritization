package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.web;

import lombok.RequiredArgsConstructor;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.AttachmentResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service.AttachmentService;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/faults/{faultId}/attachments")
@RequiredArgsConstructor
public class AttachmentController {

    private final AttachmentService service;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AttachmentResponse> upload(
            @PathVariable Long faultId,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.upload(faultId, file, principal.getUsername()));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<AttachmentResponse>> list(@PathVariable Long faultId) {
        return ResponseEntity.ok(service.listAttachments(faultId));
    }

    @GetMapping("/{attachmentId}/download")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Resource> download(
            @PathVariable Long faultId,
            @PathVariable Long attachmentId,
            @AuthenticationPrincipal UserDetails principal) {
        Resource resource = service.download(faultId, attachmentId, principal.getUsername());
        String filename = resource.getFilename() != null ? resource.getFilename() : "download";
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(filename).build().toString())
                .body(resource);
    }

    @DeleteMapping("/{attachmentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> delete(
            @PathVariable Long faultId,
            @PathVariable Long attachmentId) {
        service.delete(faultId, attachmentId);
        return ResponseEntity.noContent().build();
    }
}
