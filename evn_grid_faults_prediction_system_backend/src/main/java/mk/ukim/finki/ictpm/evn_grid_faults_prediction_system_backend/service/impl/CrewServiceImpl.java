package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service.impl;

import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.AddCrewMemberRequest;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.CreateCrewRequest;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.CrewMemberResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.CrewResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.CrewResponseDto;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.CrewSummaryResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.UpdateCrewRequest;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.exception.BadRequestException;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.exception.ConflictException;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.exception.ResourceNotFoundException;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.helpers.CrewMapper;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.helpers.CrewMemberMapper;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.Crew;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.CrewMember;
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
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service.CrewService;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service.FaultWorkflowService;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.util.CrewCodeGenerator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class CrewServiceImpl implements CrewService {

    private final CrewRepository crewRepository;
    private final FaultReportRepository faultRepo;
    private final FaultAssigmentRepository assignmentRepo;
    private final FaultWorkflowService workflowService;
    private final CrewMemberRepository crewMemberRepository;
    private final UserRepository userRepository;
    private final CrewMapper crewMapper;
    private final CrewMemberMapper crewMemberMapper;
    private final CrewCodeGenerator crewCodeGenerator;

    public CrewServiceImpl(CrewRepository crewRepository,
                           FaultReportRepository faultRepo,
                           FaultAssigmentRepository assignmentRepo,
                           FaultWorkflowService workflowService,
                           CrewMemberRepository crewMemberRepository,
                           UserRepository userRepository,
                           CrewMapper crewMapper,
                           CrewMemberMapper crewMemberMapper,
                           CrewCodeGenerator crewCodeGenerator) {
        this.crewRepository = crewRepository;
        this.faultRepo = faultRepo;
        this.assignmentRepo = assignmentRepo;
        this.workflowService = workflowService;
        this.crewMemberRepository = crewMemberRepository;
        this.userRepository = userRepository;
        this.crewMapper = crewMapper;
        this.crewMemberMapper = crewMemberMapper;
        this.crewCodeGenerator = crewCodeGenerator;
    }

    @Override
    @Deprecated
    public List<CrewResponseDto> getAll() {
        return crewRepository.findAll().stream()
                .map(c -> new CrewResponseDto(
                        c.getId(),
                        c.getName(),
                        c.getCrewMembers() != null ? c.getCrewMembers().size() : 0
                ))
                .toList();
    }

    @Override
    public CrewResponse getById(Long id) {
        Crew crew = crewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Crew", id));
        return crewMapper.toResponse(crew);
    }

    @Override
    @Deprecated
    public CrewResponseDto assignToFault(Long faultId, Long crewId) {
        FaultReport fault = faultRepo.findById(faultId)
                .orElseThrow(() -> new ResourceNotFoundException("FaultReport", faultId));
        Crew crew = crewRepository.findById(crewId)
                .orElseThrow(() -> new ResourceNotFoundException("Crew", crewId));

        FaultAssignment assignment = new FaultAssignment();
        assignment.setFaultReport(fault);
        assignment.setCrew(crew);
        assignment.setFaultStatus(FaultStatus.ASSIGNED);
        assignmentRepo.save(assignment);

        workflowService.changeStatus(fault, FaultStatus.ASSIGNED);

        return new CrewResponseDto(crew.getId(), crew.getName(),
                crew.getCrewMembers() != null ? crew.getCrewMembers().size() : 0);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CrewSummaryResponse> listAll() {
        return crewRepository.findAll().stream()
                .map(crewMapper::toSummaryResponse)
                .collect(Collectors.toList());
    }

    @Override
    public CrewResponse create(CreateCrewRequest request) {
        if (crewRepository.findByName(request.name()).isPresent()) {
            throw new ConflictException("Crew with name '" + request.name() + "' already exists");
        }

        String code = request.crewCode();
        if (code != null && !code.isBlank()) {
            if (crewRepository.existsByCrewCode(code)) {
                throw new ConflictException("Crew code '" + code + "' is already in use");
            }
        } else {
            code = crewCodeGenerator.generate();
        }

        Crew crew = new Crew();
        crew.setName(request.name());
        crew.setCrewCode(code);
        crew.setStatus(request.status() != null ? request.status() : CrewStatus.AVAILABLE);
        crew.setCurrentLatitude(request.currentLatitude());
        crew.setCurrentLongitude(request.currentLongitude());
        crew.setCrewMembers(new ArrayList<>());
        crew.setFaultAssignments(new ArrayList<>());
        crew.setInterventions(new ArrayList<>());

        return crewMapper.toResponse(crewRepository.save(crew));
    }

    @Override
    public CrewResponse update(Long id, UpdateCrewRequest request) {
        Crew crew = crewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Crew", id));

        if (request.name() != null && !request.name().isBlank()) {
            crewRepository.findByName(request.name())
                    .ifPresent(existing -> {
                        if (!existing.getId().equals(id)) {
                            throw new ConflictException("Crew with name '" + request.name() + "' already exists");
                        }
                    });
            crew.setName(request.name());
        }

        if (request.status() != null) {
            crew.setStatus(request.status());
        }
        if (request.currentLatitude() != null) {
            crew.setCurrentLatitude(request.currentLatitude());
        }
        if (request.currentLongitude() != null) {
            crew.setCurrentLongitude(request.currentLongitude());
        }

        return crewMapper.toResponse(crewRepository.save(crew));
    }

    @Override
    public void delete(Long id) {
        Crew crew = crewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Crew", id));

        if (!assignmentRepo.findByCrewId(id).isEmpty()) {
            throw new ConflictException("Cannot delete crew with assignment history. Mark as OFFLINE instead.");
        }
        if (crew.getCrewMembers() != null && !crew.getCrewMembers().isEmpty()) {
            throw new ConflictException("Remove all crew members before deleting crew.");
        }

        crewRepository.delete(crew);
    }

    @Override
    public CrewResponse addMember(Long crewId, AddCrewMemberRequest request) {
        Crew crew = crewRepository.findById(crewId)
                .orElseThrow(() -> new ResourceNotFoundException("Crew", crewId));

        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.userId()));

        if (crewMemberRepository.existsByUserId(request.userId())) {
            throw new ConflictException("User is already a member of a crew");
        }

        CrewMember member = new CrewMember();
        member.setCrew(crew);
        member.setUser(user);
        member.setFirstName(user.getFirstName());
        member.setLastName(user.getLastName());
        member.setPosition(request.position());
        member.setAssignedAt(LocalDateTime.now());

        crewMemberRepository.save(member);

        Crew updatedCrew = crewRepository.findById(crewId)
                .orElseThrow(() -> new ResourceNotFoundException("Crew", crewId));
        return crewMapper.toResponse(updatedCrew);
    }

    @Override
    public void removeMember(Long crewId, Long memberId) {
        crewRepository.findById(crewId)
                .orElseThrow(() -> new ResourceNotFoundException("Crew", crewId));

        CrewMember member = crewMemberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("CrewMember", memberId));

        if (!member.getCrew().getId().equals(crewId)) {
            throw new ConflictException("Crew member " + memberId + " does not belong to crew " + crewId);
        }

        crewMemberRepository.delete(member);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CrewResponse> getAvailable() {
        List<Crew> byStatus = crewRepository.findByStatus(CrewStatus.AVAILABLE);
        Set<Long> noActiveAssignment = crewRepository.findCrewsWithNoActiveAssignments()
                .stream().map(Crew::getId).collect(Collectors.toSet());

        return byStatus.stream()
                .filter(c -> noActiveAssignment.contains(c.getId()))
                .map(crewMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CrewMemberResponse> getMembers(Long crewId) {
        crewRepository.findById(crewId)
                .orElseThrow(() -> new ResourceNotFoundException("Crew", crewId));
        return crewMemberRepository.findByCrewId(crewId).stream()
                .map(crewMemberMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public CrewResponse updateLocation(Long crewId, Double latitude, Double longitude) {
        if (latitude < -90 || latitude > 90) {
            throw new BadRequestException("Latitude must be between -90 and 90");
        }
        if (longitude < -180 || longitude > 180) {
            throw new BadRequestException("Longitude must be between -180 and 180");
        }

        Crew crew = crewRepository.findById(crewId)
                .orElseThrow(() -> new ResourceNotFoundException("Crew", crewId));

        crew.setCurrentLatitude(latitude);
        crew.setCurrentLongitude(longitude);

        return crewMapper.toResponse(crewRepository.save(crew));
    }
}
