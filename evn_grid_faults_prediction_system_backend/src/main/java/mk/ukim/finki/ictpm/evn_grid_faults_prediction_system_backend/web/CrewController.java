package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.web;

import jakarta.validation.Valid;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.CreateCrewRequest;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.UpdateCrewRequest;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.CrewResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.CrewSummaryResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.AddCrewMemberRequest;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service.CrewService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/crews")
@Validated
public class CrewController {

    private final CrewService crewService;

    public CrewController(CrewService crewService) {
        this.crewService = crewService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('DISPATCHER', 'ADMIN')")
    public ResponseEntity<List<CrewSummaryResponse>> listAll() {
        return ResponseEntity.ok(crewService.listAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('DISPATCHER', 'ADMIN')")
    public ResponseEntity<CrewResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(crewService.getById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('DISPATCHER', 'ADMIN')")
    public ResponseEntity<CrewResponse> create(@Valid @RequestBody CreateCrewRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(crewService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('DISPATCHER', 'ADMIN')")
    public ResponseEntity<CrewResponse> update(@PathVariable Long id, @Valid @RequestBody UpdateCrewRequest request) {
        return ResponseEntity.ok(crewService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('DISPATCHER', 'ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        crewService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/available")
    @PreAuthorize("hasAnyRole('DISPATCHER', 'ADMIN')")
    public ResponseEntity<List<CrewResponse>> getAvailable() {
        return ResponseEntity.ok(crewService.getAvailable());
    }

    @PostMapping("/{id}/members")
    @PreAuthorize("hasAnyRole('DISPATCHER', 'ADMIN')")
    public ResponseEntity<CrewResponse> addMember(@PathVariable Long id, @Valid @RequestBody AddCrewMemberRequest request) {
        return ResponseEntity.ok(crewService.addMember(id, request));
    }

    @DeleteMapping("/{id}/members/{memberId}")
    @PreAuthorize("hasAnyRole('DISPATCHER', 'ADMIN')")
    public ResponseEntity<Void> removeMember(@PathVariable Long id, @PathVariable Long memberId) {
        crewService.removeMember(id, memberId);
        return ResponseEntity.noContent().build();
    }
}
