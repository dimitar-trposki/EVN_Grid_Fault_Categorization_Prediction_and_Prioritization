package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service.impl;

import lombok.extern.slf4j.Slf4j;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.request.AssignCrewRequest;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.request.ReassignCrewRequest;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.CrewRecommendationResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.FaultAssignmentResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.exception.BadRequestException;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.exception.ConflictException;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.exception.ResourceNotFoundException;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.exception.UnauthorizedException;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.helpers.FaultAssignmentMapper;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.Crew;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.FaultAssignment;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.FaultReport;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.User;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.enums.CrewStatus;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.enums.FaultStatus;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.repository.CrewMemberRepository;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.repository.CrewRepository;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.repository.FaultAssigmentRepository;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.repository.FaultReportRepository;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.repository.UserRepository;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service.AuditLogService;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service.FaultAssignmentService;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service.FaultWorkflowService;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class FaultAssignmentServiceImpl implements FaultAssignmentService {

    private static final String STATUS_ASSIGNED = "ASSIGNED";
    private static final String STATUS_REASSIGNED = "REASSIGNED";
    private static final String STATUS_ACCEPTED = "ACCEPTED";
    private static final String STATUS_COMPLETED = "COMPLETED";

    private final FaultAssigmentRepository assignmentRepo;
    private final FaultReportRepository faultReportRepo;
    private final CrewRepository crewRepo;
    private final UserRepository userRepo;
    private final CrewMemberRepository crewMemberRepo;
    private final FaultWorkflowService workflowService;
    private final FaultAssignmentMapper mapper;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;

    public FaultAssignmentServiceImpl(FaultAssigmentRepository assignmentRepo,
                                      FaultReportRepository faultReportRepo,
                                      CrewRepository crewRepo,
                                      UserRepository userRepo,
                                      CrewMemberRepository crewMemberRepo,
                                      FaultWorkflowService workflowService,
                                      FaultAssignmentMapper mapper,
                                      NotificationService notificationService,
                                      AuditLogService auditLogService) {
        this.assignmentRepo = assignmentRepo;
        this.faultReportRepo = faultReportRepo;
        this.crewRepo = crewRepo;
        this.userRepo = userRepo;
        this.crewMemberRepo = crewMemberRepo;
        this.workflowService = workflowService;
        this.mapper = mapper;
        this.notificationService = notificationService;
        this.auditLogService = auditLogService;
    }

    @Override
    public FaultAssignmentResponse assignCrew(AssignCrewRequest request, String callerEmail) {
        FaultReport fault = faultReportRepo.findById(request.faultReportId())
                .orElseThrow(() -> new ResourceNotFoundException("FaultReport", request.faultReportId()));

        Crew crew = crewRepo.findById(request.crewId())
                .orElseThrow(() -> new ResourceNotFoundException("Crew", request.crewId()));

        if (crew.getStatus() == CrewStatus.OFFLINE || crew.getStatus() == CrewStatus.MAINTENANCE) {
            throw new BadRequestException("Crew is not available for assignment (status: " + crew.getStatus() + ")");
        }

        User assignedBy = userRepo.findByEmail(callerEmail).orElse(null);

        FaultAssignment assignment = new FaultAssignment();
        assignment.setFaultReport(fault);
        assignment.setCrew(crew);
        assignment.setAssignedByUser(assignedBy);
        assignment.setAssignedAt(LocalDateTime.now());
        assignment.setAssignmentStatus(STATUS_ASSIGNED);
        assignment.setAssignmentNote(request.note());
        assignment.setFaultStatus(FaultStatus.ASSIGNED);

        FaultAssignment saved = assignmentRepo.save(assignment);

        // Update fault status
        try {
            workflowService.changeStatus(fault, FaultStatus.ASSIGNED);
        } catch (Exception e) {
            log.warn("Failed to update fault status after assignment: {}", e.getMessage());
        }

        // Crew shifts from AVAILABLE to EN_ROUTE upon assignment
        try {
            crew.setStatus(CrewStatus.EN_ROUTE);
            crewRepo.save(crew);
        } catch (Exception e) {
            log.warn("Failed to update crew status to EN_ROUTE: {}", e.getMessage());
        }

        // Audit log
        try {
            auditLogService.log("FaultAssignment", saved.getId(), "ASSIGN", null,
                    "crewId=" + crew.getId() + ", faultId=" + fault.getId());
        } catch (Exception e) {
            log.warn("Audit log failed for ASSIGN assignmentId={}: {}", saved.getId(), e.getMessage());
        }

        // Notify crew members
        try {
            notifyCrewMembers(crew, "New task assigned",
                    "You have been assigned to fault " + fault.getTrackingCode() + ": " + fault.getTitle());
        } catch (Exception e) {
            log.warn("Crew assignment notification failed for crewId={}: {}", crew.getId(), e.getMessage());
        }

        return mapper.toResponse(saved);
    }

    @Override
    public FaultAssignmentResponse reassignCrew(Long faultReportId, ReassignCrewRequest request, String callerEmail) {
        FaultAssignment current = assignmentRepo.findFirstByFaultReportIdOrderByAssignedAtDesc(faultReportId)
                .orElseThrow(() -> new ResourceNotFoundException("No assignment found for fault " + faultReportId));

        Crew oldCrew = current.getCrew();
        Long oldCrewId = oldCrew.getId();

        // Mark current assignment as REASSIGNED
        current.setAssignmentStatus(STATUS_REASSIGNED);
        current.setCompletedAt(LocalDateTime.now());
        assignmentRepo.save(current);

        // Revert old crew status if it was EN_ROUTE
        try {
            if (oldCrew.getStatus() == CrewStatus.EN_ROUTE) {
                oldCrew.setStatus(CrewStatus.AVAILABLE);
                crewRepo.save(oldCrew);
            }
        } catch (Exception e) {
            log.warn("Failed to revert old crew status: {}", e.getMessage());
        }

        // Notify old crew that assignment was moved away
        try {
            FaultReport fault = current.getFaultReport();
            notifyCrewMembers(oldCrew, "Task reassigned away",
                    "The task for fault " + fault.getTrackingCode() + " has been reassigned to another crew.");
        } catch (Exception e) {
            log.warn("Old crew reassignment notification failed for crewId={}: {}", oldCrewId, e.getMessage());
        }

        // Audit old assignment
        try {
            auditLogService.log("FaultAssignment", current.getId(), "REASSIGN",
                    "crewId=" + oldCrewId, "crewId=" + request.newCrewId());
        } catch (Exception e) {
            log.warn("Audit log failed for REASSIGN assignmentId={}: {}", current.getId(), e.getMessage());
        }

        // Create new assignment (also sends "New task assigned" notification to new crew)
        AssignCrewRequest newAssignRequest = new AssignCrewRequest(faultReportId, request.newCrewId(), request.note());
        return assignCrew(newAssignRequest, callerEmail);
    }

    @Override
    public FaultAssignmentResponse acceptAssignment(Long assignmentId, String callerEmail) {
        FaultAssignment assignment = assignmentRepo.findById(assignmentId)
                .orElseThrow(() -> new ResourceNotFoundException("FaultAssignment", assignmentId));

        Long crewId = assignment.getCrew().getId();
        boolean isMember = crewMemberRepo.findByUserId(
                userRepo.findByEmail(callerEmail).map(User::getId).orElse(-1L)
        ).map(m -> m.getCrew().getId().equals(crewId)).orElse(false);

        if (!isMember) {
            throw new UnauthorizedException("Only a member of the assigned crew can accept this assignment");
        }

        if (!STATUS_ASSIGNED.equals(assignment.getAssignmentStatus())) {
            throw new BadRequestException("Assignment is not in ASSIGNED state");
        }

        assignment.setAcceptedAt(LocalDateTime.now());
        assignment.setAssignmentStatus(STATUS_ACCEPTED);

        // Crew stays EN_ROUTE — ON_SITE only set when intervention starts
        return mapper.toResponse(assignmentRepo.save(assignment));
    }

    @Override
    @Transactional(readOnly = true)
    public FaultAssignmentResponse getByFault(Long faultReportId) {
        return assignmentRepo.findFirstByFaultReportIdOrderByAssignedAtDesc(faultReportId)
                .map(mapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("No assignment found for fault " + faultReportId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<FaultAssignmentResponse> getByCrew(Long crewId) {
        crewRepo.findById(crewId).orElseThrow(() -> new ResourceNotFoundException("Crew", crewId));
        return assignmentRepo.findByCrewId(crewId).stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CrewRecommendationResponse> recommendCrews(Long faultReportId) {
        // TODO: Optional enhancement — delegate to AiClient if a /recommend-crew endpoint is added later.
        FaultReport fault = faultReportRepo.findById(faultReportId)
                .orElseThrow(() -> new ResourceNotFoundException("FaultReport", faultReportId));

        Double faultLat = fault.getLocation() != null ? fault.getLocation().getLatitude() : null;
        Double faultLng = fault.getLocation() != null ? fault.getLocation().getLongitude() : null;

        List<Crew> availableCrews = crewRepo.findByStatus(CrewStatus.AVAILABLE);

        List<CrewRecommendationResponse> recommendations = new ArrayList<>();
        for (Crew crew : availableCrews) {
            double distanceKm = computeDistance(faultLat, faultLng, crew.getCurrentLatitude(), crew.getCurrentLongitude());
            double score = computeScore(distanceKm);
            String reason = buildReason(crew, distanceKm);
            recommendations.add(new CrewRecommendationResponse(
                    crew.getId(),
                    crew.getCrewCode(),
                    crew.getName(),
                    score,
                    reason,
                    distanceKm
            ));
        }

        // Sort by score descending
        recommendations.sort(Comparator.comparingDouble(CrewRecommendationResponse::score).reversed());
        return recommendations;
    }

    private double computeDistance(Double faultLat, Double faultLng, Double crewLat, Double crewLng) {
        if (faultLat == null || faultLng == null || crewLat == null || crewLng == null) {
            return 999.0; // default penalty for unknown position
        }
        // Haversine formula
        final double R = 6371.0;
        double dLat = Math.toRadians(crewLat - faultLat);
        double dLng = Math.toRadians(crewLng - faultLng);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(faultLat)) * Math.cos(Math.toRadians(crewLat))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    private double computeScore(double distanceKm) {
        if (distanceKm >= 999.0) return 10.0; // unknown position penalty
        // Score 100 at 0 km, decays to 0 at 200 km
        return Math.max(0.0, 100.0 - (distanceKm / 2.0));
    }

    private String buildReason(Crew crew, double distanceKm) {
        if (distanceKm >= 999.0) {
            return "Crew location unknown — ranked by availability";
        }
        return String.format("%.1f km from fault location", distanceKm);
    }

    private void notifyCrewMembers(Crew crew, String title, String message) {
        crewMemberRepo.findByCrewId(crew.getId()).forEach(member -> {
            if (member.getUser() != null) {
                notificationService.sendToUser(member.getUser().getId(), title, message, "ASSIGNMENT");
            }
        });
    }
}
