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

import java.util.List;

@Service
public class CrewServiceImpl implements CrewService {

    private final CrewRepository crewRepo;
    private final FaultReportRepository faultRepo;
    private final FaultAssigmentRepository assignmentRepo;
    private final FaultWorkflowService workflowService;

    public CrewServiceImpl(CrewRepository crewRepo,
                           FaultReportRepository faultRepo,
                           FaultAssigmentRepository assignmentRepo,
                           FaultWorkflowService workflowService) {
        this.crewRepo = crewRepo;
        this.faultRepo = faultRepo;
        this.assignmentRepo = assignmentRepo;
        this.workflowService = workflowService;
    }

    @Override
    public List<CrewResponseDto> getAll() {
        return crewRepo.findAll().stream()
                .map(c -> new CrewResponseDto(
                        c.getId(),
                        c.getName(),
                        c.getCrewMembers().size()
                ))
                .toList();
    }

    @Override
    public CrewResponseDto getById(Long id) {
        Crew crew = crewRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Crew not found: " + id));
        return new CrewResponseDto(crew.getId(), crew.getName(), crew.getCrewMembers().size());
    }

    @Override
    public CrewResponseDto assignToFault(Long faultId, Long crewId) {
        FaultReport fault = faultRepo.findById(faultId)
                .orElseThrow(() -> new RuntimeException("Fault not found: " + faultId));
        Crew crew = crewRepo.findById(crewId)
                .orElseThrow(() -> new RuntimeException("Crew not found: " + crewId));

        FaultAssignment assignment = new FaultAssignment();
        assignment.setFaultReport(fault);
        assignment.setCrew(crew);
        assignment.setFaultStatus(FaultStatus.ASSIGNED);
        assignmentRepo.save(assignment);

        workflowService.changeStatus(fault, FaultStatus.ASSIGNED);

        return new CrewResponseDto(crew.getId(), crew.getName(), crew.getCrewMembers().size());
    }
}
