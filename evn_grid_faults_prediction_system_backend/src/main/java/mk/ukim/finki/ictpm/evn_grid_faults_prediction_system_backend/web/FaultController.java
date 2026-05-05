package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.web;

import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.ChangeStatusDto;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.CreateFaultReportDto;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service.FaultReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/faults")
public class FaultController {

    private final FaultReportService service;

    public FaultController(FaultReportService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody CreateFaultReportDto dto) {
        return ResponseEntity.ok(service.createFault(dto));
    }

    @GetMapping
    public ResponseEntity<?> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOne(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> changeStatus(
            @PathVariable Long id,
            @RequestBody ChangeStatusDto dto
    ) {
        return ResponseEntity.ok(service.changeStatus(id, dto.getStatus()));
    }
}