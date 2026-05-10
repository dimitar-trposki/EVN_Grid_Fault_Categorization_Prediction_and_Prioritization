package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.web;

import jakarta.validation.Valid;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.AddCrewMemberRequest;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.CreateCrewRequest;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.CrewMemberResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.CrewResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.CrewSummaryResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.UpdateCrewRequest;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.request.UpdateCrewLocationRequest;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.exception.UnauthorizedException;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.User;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.repository.CrewMemberRepository;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service.CrewService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/crews")
@Validated
public class CrewController {

    private final CrewService crewService;
    private final CrewMemberRepository crewMemberRepository;

    public CrewController(CrewService crewService, CrewMemberRepository crewMemberRepository) {
        this.crewService = crewService;
        this.crewMemberRepository = crewMemberRepository;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('OPERATOR', 'DISPATCHER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<List<CrewSummaryResponse>> listAll() {
        return ResponseEntity.ok(crewService.listAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('OPERATOR', 'DISPATCHER', 'MANAGER', 'ADMIN')")
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
    public ResponseEntity<CrewResponse> update(@PathVariable Long id,
                                               @Valid @RequestBody UpdateCrewRequest request) {
        return ResponseEntity.ok(crewService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        crewService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/available")
    @PreAuthorize("hasAnyRole('OPERATOR', 'DISPATCHER', 'ADMIN')")
    public ResponseEntity<List<CrewResponse>> getAvailable() {
        return ResponseEntity.ok(crewService.getAvailable());
    }

    @PostMapping("/{id}/members")
    @PreAuthorize("hasAnyRole('DISPATCHER', 'ADMIN')")
    public ResponseEntity<CrewResponse> addMember(@PathVariable Long id,
                                                  @Valid @RequestBody AddCrewMemberRequest request) {
        return ResponseEntity.ok(crewService.addMember(id, request));
    }

    @DeleteMapping("/{id}/members/{memberId}")
    @PreAuthorize("hasAnyRole('DISPATCHER', 'ADMIN')")
    public ResponseEntity<Void> removeMember(@PathVariable Long id, @PathVariable Long memberId) {
        crewService.removeMember(id, memberId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/members")
    @PreAuthorize("hasAnyRole('OPERATOR', 'DISPATCHER', 'MANAGER', 'ADMIN')")
    public ResponseEntity<List<CrewMemberResponse>> getMembers(@PathVariable Long id) {
        return ResponseEntity.ok(crewService.getMembers(id));
    }

    @PutMapping("/{id}/location")
    @PreAuthorize("hasAnyRole('FIELD_CREW', 'DISPATCHER', 'ADMIN')")
    public ResponseEntity<CrewResponse> updateLocation(@PathVariable Long id,
                                                       @Valid @RequestBody UpdateCrewLocationRequest request,
                                                       @AuthenticationPrincipal User principal) {
        if (principal != null && principal.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_FIELD_CREW"))) {
            boolean isMember = crewMemberRepository.findByUserId(principal.getId())
                    .map(m -> m.getCrew().getId().equals(id))
                    .orElse(false);
            if (!isMember) {
                throw new UnauthorizedException("You are not a member of this crew");
            }
        }
        return ResponseEntity.ok(crewService.updateLocation(id, request.latitude(), request.longitude()));
    }

    @PostMapping("/{crewId}/assign/{faultId}")
    @PreAuthorize("hasAnyRole('DISPATCHER', 'ADMIN')")
    public ResponseEntity<?> assign(@PathVariable Long crewId, @PathVariable Long faultId) {
        return ResponseEntity.ok(crewService.assignToFault(faultId, crewId));
    }
}
