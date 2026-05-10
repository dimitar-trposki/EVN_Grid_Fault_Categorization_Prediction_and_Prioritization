package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service.impl;

import lombok.extern.slf4j.Slf4j;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.InterventionCreateDto;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.InterventionResponseDto;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.request.CloseFaultRequest;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.request.StartInterventionRequest;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.request.UpdateInterventionRequest;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.InterventionResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.InterventionSummaryResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.exception.BadRequestException;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.exception.ResourceNotFoundException;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.exception.UnauthorizedException;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.helpers.InterventionMapper;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.Crew;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.FaultReport;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.Intervention;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.User;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.enums.CrewStatus;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.enums.FaultStatus;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.enums.RoleType;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.repository.CrewMemberRepository;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.repository.CrewRepository;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.repository.FaultReportRepository;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.repository.FaultStatusHistoryRepository;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.repository.InterventionRepository;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.repository.UserRepository;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service.AuditLogService;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service.FaultWorkflowService;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service.InterventionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class InterventionServiceImpl implements InterventionService {

    private static final String RESOLUTION_IN_PROGRESS = "IN_PROGRESS";
    private static final String RESOLUTION_RESOLVED = "RESOLVED";

    private final InterventionRepository interventionRepo;
    private final FaultReportRepository faultRepo;
    private final CrewRepository crewRepo;
    private final UserRepository userRepo;
    private final CrewMemberRepository crewMemberRepo;
    private final FaultStatusHistoryRepository statusHistoryRepo;
    private final FaultWorkflowService workflowService;
    private final InterventionMapper mapper;
    private final AuditLogService auditLogService;

    public InterventionServiceImpl(InterventionRepository interventionRepo,
                                   FaultReportRepository faultRepo,
                                   CrewRepository crewRepo,
                                   UserRepository userRepo,
                                   CrewMemberRepository crewMemberRepo,
                                   FaultStatusHistoryRepository statusHistoryRepo,
                                   FaultWorkflowService workflowService,
                                   InterventionMapper mapper,
                                   AuditLogService auditLogService) {
        this.interventionRepo = interventionRepo;
        this.faultRepo = faultRepo;
        this.crewRepo = crewRepo;
        this.userRepo = userRepo;
        this.crewMemberRepo = crewMemberRepo;
        this.statusHistoryRepo = statusHistoryRepo;
        this.workflowService = workflowService;
        this.mapper = mapper;
        this.auditLogService = auditLogService;
    }

    @Override
    @Deprecated
    public InterventionResponseDto create(Long faultId, InterventionCreateDto dto) {
        FaultReport fault = faultRepo.findById(faultId)
                .orElseThrow(() -> new ResourceNotFoundException("FaultReport", faultId));
        Crew crew = crewRepo.findById(dto.getCrewId())
                .orElseThrow(() -> new ResourceNotFoundException("Crew", dto.getCrewId()));

        Intervention intervention = new Intervention();
        intervention.setDescription(dto.getDescription());
        intervention.setFaultReport(fault);
        intervention.setCrew(crew);
        intervention.setStartedAt(LocalDateTime.now());
        intervention.setResolutionStatus(RESOLUTION_IN_PROGRESS);
        intervention = interventionRepo.save(intervention);

        workflowService.changeStatus(fault, FaultStatus.IN_PROGRESS);

        return new InterventionResponseDto(intervention.getId(), intervention.getDescription(), crew.getName());
    }

    @Override
    @Deprecated
    public List<InterventionResponseDto> getByFault(Long faultId) {
        return interventionRepo.findAllByFaultReportId(faultId).stream()
                .map(i -> new InterventionResponseDto(i.getId(), i.getDescription(), i.getCrew().getName()))
                .toList();
    }

    @Override
    public InterventionResponse start(StartInterventionRequest request, String callerEmail) {
        FaultReport fault = faultRepo.findById(request.faultReportId())
                .orElseThrow(() -> new ResourceNotFoundException("FaultReport", request.faultReportId()));

        // Validate fault is in ASSIGNED or IN_PROGRESS
        FaultStatus currentStatus = statusHistoryRepo
                .findByFaultReportIdOrderByChangedAtDesc(fault.getId())
                .stream().findFirst()
                .map(h -> h.getFaultStatus())
                .orElse(FaultStatus.REPORTED);

        if (currentStatus != FaultStatus.ASSIGNED && currentStatus != FaultStatus.IN_PROGRESS) {
            throw new BadRequestException(
                    "Fault must be in ASSIGNED or IN_PROGRESS state to start an intervention. Current: " + currentStatus);
        }

        Crew crew = crewRepo.findById(request.crewId())
                .orElseThrow(() -> new ResourceNotFoundException("Crew", request.crewId()));

        Intervention intervention = new Intervention();
        intervention.setFaultReport(fault);
        intervention.setCrew(crew);
        intervention.setLocation(fault.getLocation());
        intervention.setStartedAt(LocalDateTime.now());
        intervention.setResolutionStatus(RESOLUTION_IN_PROGRESS);
        intervention.setDescription("Intervention started");

        Intervention saved = interventionRepo.save(intervention);

        // Update fault status to IN_PROGRESS
        try {
            workflowService.changeStatus(fault, FaultStatus.IN_PROGRESS);
        } catch (Exception e) {
            log.warn("Failed to update fault status to IN_PROGRESS: {}", e.getMessage());
        }

        // Crew shifts to ON_SITE when physical work begins
        try {
            crew.setStatus(CrewStatus.ON_SITE);
            crewRepo.save(crew);
        } catch (Exception e) {
            log.warn("Failed to update crew status to ON_SITE: {}", e.getMessage());
        }

        return mapper.toResponse(saved);
    }

    @Override
    public InterventionResponse update(Long id, UpdateInterventionRequest request, String callerEmail) {
        Intervention intervention = interventionRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Intervention", id));

        checkCrewMembership(callerEmail, intervention.getCrew().getId());

        if (request.resolutionStatus() != null) {
            intervention.setResolutionStatus(request.resolutionStatus());
        }
        if (request.resolutionNotes() != null) {
            intervention.setResolutionNotes(request.resolutionNotes());
        }
        if (request.rootCause() != null) {
            intervention.setRootCause(request.rootCause());
        }

        return mapper.toResponse(interventionRepo.save(intervention));
    }

    @Override
    public InterventionResponse closeFault(Long faultReportId, CloseFaultRequest request, String callerEmail) {
        Intervention intervention = interventionRepo.findByFaultReportId(faultReportId)
                .orElseThrow(() -> new ResourceNotFoundException("No intervention found for fault " + faultReportId));

        checkCrewMembership(callerEmail, intervention.getCrew().getId());

        if (RESOLUTION_RESOLVED.equals(intervention.getResolutionStatus())) {
            throw new BadRequestException("Intervention is already resolved");
        }

        LocalDateTime endedAt = LocalDateTime.now();
        intervention.setEndedAt(endedAt);
        if (intervention.getStartedAt() != null) {
            intervention.setDurationMinutes((int) ChronoUnit.MINUTES.between(intervention.getStartedAt(), endedAt));
        }
        intervention.setResolutionStatus(RESOLUTION_RESOLVED);
        if (request.resolutionNotes() != null) {
            intervention.setResolutionNotes(request.resolutionNotes());
        }
        if (request.rootCause() != null) {
            intervention.setRootCause(request.rootCause());
        }

        Intervention saved = interventionRepo.save(intervention);

        // Update fault to RESOLVED
        FaultReport fault = intervention.getFaultReport();
        try {
            workflowService.changeStatus(fault, FaultStatus.RESOLVED);
        } catch (Exception e) {
            log.warn("Failed to update fault status to RESOLVED: {}", e.getMessage());
        }

        // Return crew to AVAILABLE
        try {
            Crew crew = intervention.getCrew();
            crew.setStatus(CrewStatus.AVAILABLE);
            crewRepo.save(crew);
        } catch (Exception e) {
            log.warn("Failed to return crew status to AVAILABLE: {}", e.getMessage());
        }

        // Audit log
        try {
            String rootCause = request.rootCause() != null ? request.rootCause() : "N/A";
            auditLogService.log("FaultReport", fault.getId(), "CLOSE", null,
                    "rootCause=" + rootCause);
        } catch (Exception e) {
            log.warn("Audit log failed for CLOSE faultId={}: {}", fault.getId(), e.getMessage());
        }

        return mapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public InterventionResponse getById(Long id) {
        return mapper.toResponse(interventionRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Intervention", id)));
    }

    @Override
    @Transactional(readOnly = true)
    public InterventionResponse getByFaultReport(Long faultReportId) {
        return mapper.toResponse(interventionRepo.findByFaultReportId(faultReportId)
                .orElseThrow(() -> new ResourceNotFoundException("No intervention found for fault " + faultReportId)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<InterventionSummaryResponse> getByCrew(Long crewId) {
        crewRepo.findById(crewId).orElseThrow(() -> new ResourceNotFoundException("Crew", crewId));
        return interventionRepo.findByCrewId(crewId).stream()
                .map(mapper::toSummaryResponse)
                .collect(Collectors.toList());
    }

    private void checkCrewMembership(String callerEmail, Long crewId) {
        User caller = userRepo.findByEmail(callerEmail).orElse(null);
        if (caller == null) return;
        if (caller.getUserRole() != RoleType.FIELD_CREW) return; // DISPATCHER/ADMIN bypass

        boolean isMember = crewMemberRepo.findByUserId(caller.getId())
                .map(m -> m.getCrew().getId().equals(crewId))
                .orElse(false);
        if (!isMember) {
            throw new UnauthorizedException("You are not a member of the crew assigned to this intervention");
        }
    }
}
