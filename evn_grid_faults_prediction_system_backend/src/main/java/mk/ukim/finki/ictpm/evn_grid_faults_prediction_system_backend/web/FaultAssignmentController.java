package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.web;

import jakarta.validation.Valid;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.request.AssignCrewRequest;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.request.ReassignCrewRequest;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.CrewRecommendationResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.FaultAssignmentResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service.FaultAssignmentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/assignments")
@Validated
public class FaultAssignmentController {

    private final FaultAssignmentService assignmentService;

    public FaultAssignmentController(FaultAssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('OPERATOR', 'DISPATCHER', 'ADMIN')")
    public ResponseEntity<FaultAssignmentResponse> assignCrew(
            @Valid @RequestBody AssignCrewRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(assignmentService.assignCrew(request, principal.getUsername()));
    }

    @PutMapping("/fault/{faultId}/reassign")
    @PreAuthorize("hasAnyRole('DISPATCHER', 'ADMIN')")
    public ResponseEntity<FaultAssignmentResponse> reassignCrew(
            @PathVariable Long faultId,
            @Valid @RequestBody ReassignCrewRequest request,
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(assignmentService.reassignCrew(faultId, request, principal.getUsername()));
    }

    @PutMapping("/{id}/accept")
    @PreAuthorize("hasRole('FIELD_CREW')")
    public ResponseEntity<FaultAssignmentResponse> acceptAssignment(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(assignmentService.acceptAssignment(id, principal.getUsername()));
    }

    @GetMapping("/fault/{faultId}")
    @PreAuthorize("hasAnyRole('OPERATOR', 'DISPATCHER', 'MANAGER', 'ADMIN', 'FIELD_CREW')")
    public ResponseEntity<FaultAssignmentResponse> getByFault(@PathVariable Long faultId) {
        return ResponseEntity.ok(assignmentService.getByFault(faultId));
    }

    @GetMapping("/crew/{crewId}")
    @PreAuthorize("hasAnyRole('DISPATCHER', 'MANAGER', 'ADMIN', 'FIELD_CREW')")
    public ResponseEntity<List<FaultAssignmentResponse>> getByCrew(@PathVariable Long crewId) {
        return ResponseEntity.ok(assignmentService.getByCrew(crewId));
    }

    @GetMapping("/recommendations/{faultId}")
    @PreAuthorize("hasAnyRole('OPERATOR', 'DISPATCHER', 'ADMIN')")
    public ResponseEntity<List<CrewRecommendationResponse>> recommendCrews(@PathVariable Long faultId) {
        return ResponseEntity.ok(assignmentService.recommendCrews(faultId));
    }
}
