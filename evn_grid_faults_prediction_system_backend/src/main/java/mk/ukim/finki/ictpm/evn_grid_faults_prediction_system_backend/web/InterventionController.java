package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.web;

import jakarta.validation.Valid;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.request.CloseFaultRequest;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.request.StartInterventionRequest;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.request.UpdateInterventionRequest;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.InterventionResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.InterventionSummaryResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service.InterventionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/interventions")
@Validated
public class InterventionController {

    private final InterventionService interventionService;

    public InterventionController(InterventionService interventionService) {
        this.interventionService = interventionService;
    }

    @PostMapping("/start")
    @PreAuthorize("hasAnyRole('FIELD_CREW', 'DISPATCHER', 'ADMIN')")
    public ResponseEntity<InterventionResponse> start(
            @Valid @RequestBody StartInterventionRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(interventionService.start(request, principal.getUsername()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('FIELD_CREW', 'DISPATCHER', 'ADMIN')")
    public ResponseEntity<InterventionResponse> update(
            @PathVariable Long id,
            @RequestBody UpdateInterventionRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(interventionService.update(id, request, principal.getUsername()));
    }

    @PutMapping("/fault/{faultId}/close")
    @PreAuthorize("hasAnyRole('FIELD_CREW', 'DISPATCHER', 'ADMIN')")
    public ResponseEntity<InterventionResponse> closeFault(
            @PathVariable Long faultId,
            @RequestBody CloseFaultRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(interventionService.closeFault(faultId, request, principal.getUsername()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<InterventionResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(interventionService.getById(id));
    }

    @GetMapping("/fault/{faultId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<InterventionResponse> getByFault(@PathVariable Long faultId) {
        return ResponseEntity.ok(interventionService.getByFaultReport(faultId));
    }

    @GetMapping("/crew/{crewId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<InterventionSummaryResponse>> getByCrew(@PathVariable Long crewId) {
        return ResponseEntity.ok(interventionService.getByCrew(crewId));
    }
}
