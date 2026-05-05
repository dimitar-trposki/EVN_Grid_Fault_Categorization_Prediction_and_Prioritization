package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.web;

import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service.AttachmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/attachments")
public class AttachmentController {

    private final AttachmentService service;

    public AttachmentController(AttachmentService service) {
        this.service = service;
    }

    @PostMapping("/{faultId}")
    public ResponseEntity<?> upload(
            @PathVariable Long faultId,
            @RequestParam MultipartFile file
    ) {
        return ResponseEntity.ok(service.upload(faultId, file));
    }

    @GetMapping("/{faultId}")
    public ResponseEntity<?> getAll(@PathVariable Long faultId) {
        return ResponseEntity.ok(service.getByFault(faultId));
    }
}