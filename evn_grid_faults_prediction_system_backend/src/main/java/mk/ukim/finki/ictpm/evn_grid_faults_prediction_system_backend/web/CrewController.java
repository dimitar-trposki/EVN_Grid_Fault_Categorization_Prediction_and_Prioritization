package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.web;

import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service.CrewService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/crews")
public class CrewController {

    private final CrewService crewService;

    public CrewController(CrewService crewService) {
        this.crewService = crewService;
    }

    @GetMapping
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(crewService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOne(@PathVariable Long id) {
        return ResponseEntity.ok(crewService.getById(id));
    }

    @PostMapping("/{crewId}/assign/{faultId}")
    public ResponseEntity<?> assign(@PathVariable Long crewId,
                                    @PathVariable Long faultId) {
        return ResponseEntity.ok(crewService.assignToFault(faultId, crewId));
    }
}