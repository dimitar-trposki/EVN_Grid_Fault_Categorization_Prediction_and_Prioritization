package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.web;

import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.InterventionCreateDto;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service.InterventionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/interventions")
public class InterventionController {

    private final InterventionService interventionService;

    public InterventionController(InterventionService interventionService) {
        this.interventionService = interventionService;
    }

    @PostMapping("/{faultId}")
    public ResponseEntity<?> create(@PathVariable Long faultId,
                                    @RequestBody InterventionCreateDto dto) {
        return ResponseEntity.ok(interventionService.create(faultId, dto));
    }

    @GetMapping("/{faultId}")
    public ResponseEntity<?> getByFault(@PathVariable Long faultId) {
        return ResponseEntity.ok(interventionService.getByFault(faultId));
    }
}