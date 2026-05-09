package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service.impl;

import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.CrewResponseDto;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.Crew;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.FaultAssignment;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.FaultReport;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.enums.FaultStatus;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.repository.CrewRepository;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.repository.FaultAssigmentRepository;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.repository.FaultReportRepository;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service.CrewService;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service.FaultWorkflowService;
import org.springframework.stereotype.Service;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.CreateCrewRequest;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.UpdateCrewRequest;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.CrewResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.CrewSummaryResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.AddCrewMemberRequest;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.exception.ConflictException;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.exception.ResourceNotFoundException;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.helpers.CrewMapper;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.CrewMember;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.User;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.repository.CrewMemberRepository;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
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

    public CrewServiceImpl(CrewRepository crewRepository,
                           FaultReportRepository faultRepo,
                           FaultAssigmentRepository assignmentRepo,
                           FaultWorkflowService workflowService,
                           CrewMemberRepository crewMemberRepository,
                           UserRepository userRepository,
                           CrewMapper crewMapper) {
        this.crewRepository = crewRepository;
        this.faultRepo = faultRepo;
        this.assignmentRepo = assignmentRepo;
        this.workflowService = workflowService;
        this.crewMemberRepository = crewMemberRepository;
        this.userRepository = userRepository;
        this.crewMapper = crewMapper;
    }

    @Override
    public List<CrewResponseDto> getAll() {
        return crewRepository.findAll().stream()
                .map(c -> new CrewResponseDto(
                        c.getId(),
                        c.getName(),
                        c.getCrewMembers().size()
                ))
                .toList();
    }

    @Override
    public CrewResponseDto getById(Long id) {
        Crew crew = crewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Crew not found: " + id));
        return new CrewResponseDto(crew.getId(), crew.getName(), crew.getCrewMembers().size());
    }

    @Override
    public CrewResponseDto assignToFault(Long faultId, Long crewId) {
        FaultReport fault = faultRepo.findById(faultId)
                .orElseThrow(() -> new RuntimeException("Fault not found: " + faultId));
        Crew crew = crewRepository.findById(crewId)
                .orElseThrow(() -> new RuntimeException("Crew not found: " + crewId));

        FaultAssignment assignment = new FaultAssignment();
        assignment.setFaultReport(fault);
        assignment.setCrew(crew);
        assignment.setFaultStatus(FaultStatus.ASSIGNED);
        assignmentRepo.save(assignment);

        workflowService.changeStatus(fault, FaultStatus.ASSIGNED);

        return new CrewResponseDto(crew.getId(), crew.getName(), crew.getCrewMembers().size());
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
        Crew crew = new Crew();
        crew.setName(request.name());
        crew.setCrewMembers(new ArrayList<>());
        crew.setFaultAssignments(new ArrayList<>());
        crew.setInterventions(new ArrayList<>());
        Crew savedCrew = crewRepository.save(crew);
        return crewMapper.toResponse(savedCrew);
    }

    @Override
    public CrewResponse update(Long id, UpdateCrewRequest request) {
        Crew crew = crewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Crew with id " + id + " not found"));

        crewRepository.findByName(request.name())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(id)) {
                        throw new ConflictException("Crew with name '" + request.name() + "' already exists");
                    }
                });

        crew.setName(request.name());
        Crew updatedCrew = crewRepository.save(crew);
        return crewMapper.toResponse(updatedCrew);
    }

    @Override
    public void delete(Long id) {
        Crew crew = crewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Crew with id " + id + " not found"));
        crewRepository.delete(crew);
    }

    @Override
    public CrewResponse addMember(Long crewId, AddCrewMemberRequest request) {
        Crew crew = crewRepository.findById(crewId)
                .orElseThrow(() -> new ResourceNotFoundException("Crew with id " + crewId + " not found"));

        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new ResourceNotFoundException("User with id " + request.userId() + " not found"));

        if (crewMemberRepository.findByUserId(request.userId()).isPresent()) {
            throw new ConflictException("User is already a member of a crew");
        }

        CrewMember member = new CrewMember();
        member.setCrew(crew);
        member.setUser(user);
        member.setFirstName(user.getFirstName());
        member.setLastName(user.getLastName());
        member.setPosition(request.position());

        crewMemberRepository.save(member);

        Crew updatedCrew = crewRepository.findById(crewId)
                .orElseThrow(() -> new ResourceNotFoundException("Crew with id " + crewId + " not found"));
        return crewMapper.toResponse(updatedCrew);
    }

    @Override
    public void removeMember(Long crewId, Long memberId) {
        Crew crew = crewRepository.findById(crewId)
                .orElseThrow(() -> new ResourceNotFoundException("Crew with id " + crewId + " not found"));

        CrewMember member = crewMemberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Crew member with id " + memberId + " not found"));

        if (!member.getCrew().getId().equals(crewId)) {
            throw new ConflictException("Crew member with id " + memberId + " does not belong to crew with id " + crewId);
        }

        crewMemberRepository.delete(member);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CrewResponse> getAvailable() {
        return crewRepository.findAvailableCrews().stream()
                .map(crewMapper::toResponse)
                .collect(Collectors.toList());
    }
}
