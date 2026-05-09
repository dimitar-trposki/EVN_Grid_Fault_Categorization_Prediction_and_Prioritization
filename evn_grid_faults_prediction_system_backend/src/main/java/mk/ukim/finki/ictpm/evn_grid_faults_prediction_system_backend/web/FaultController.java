package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.request.CreateFaultReportRequest;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.request.FaultStatusUpdateRequest;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.request.OperatorCreateFaultRequest;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.request.UpdateFaultReportRequest;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.FaultReportResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.FaultReportSummaryResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.FaultStatusHistoryResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.TrackFaultResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.enums.FaultClassification;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.enums.FaultPriority;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.enums.FaultStatus;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.enums.FaultType;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service.FaultReportService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/faults")
@RequiredArgsConstructor
public class FaultController {

    private final FaultReportService service;

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<FaultReportResponse> createByCustomer(
            @RequestBody @Valid CreateFaultReportRequest dto,
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.createByCustomer(dto, principal.getUsername()));
    }

    @PostMapping("/operator")
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN', 'MANAGER')")
    public ResponseEntity<FaultReportResponse> createByOperator(
            @RequestBody @Valid OperatorCreateFaultRequest dto,
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.createByOperator(dto, principal.getUsername()));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR', 'MANAGER', 'DISPATCHER')")
    public ResponseEntity<Page<FaultReportSummaryResponse>> getFiltered(
            @RequestParam(required = false) FaultType faultType,
            @RequestParam(required = false) FaultPriority faultPriority,
            @RequestParam(required = false) FaultClassification faultClassification,
            @RequestParam(required = false) FaultStatus status,
            @RequestParam(required = false) Long locationId,
            @RequestParam(required = false) Long regionId,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "reportedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        return ResponseEntity.ok(service.getFiltered(
                faultType, faultPriority, faultClassification, status,
                locationId, regionId, customerId, from, to,
                PageRequest.of(page, size, sort)));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<FaultReportSummaryResponse>> getMyFaults(
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(service.getMyFaults(principal.getUsername()));
    }

    @GetMapping("/track/{code}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<TrackFaultResponse> track(@PathVariable String code) {
        return ResponseEntity.ok(service.getByTrackingCode(code));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<FaultReportResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getFaultById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ResponseEntity<FaultReportResponse> update(
            @PathVariable Long id,
            @RequestBody @Valid UpdateFaultReportRequest dto) {
        return ResponseEntity.ok(service.updateFault(id, dto));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> updateStatus(
            @PathVariable Long id,
            @RequestBody @Valid FaultStatusUpdateRequest dto,
            @AuthenticationPrincipal UserDetails principal) {
        service.updateStatus(id, dto, principal.getUsername());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/history")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<FaultStatusHistoryResponse>> getHistory(@PathVariable Long id) {
        return ResponseEntity.ok(service.getStatusHistory(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
