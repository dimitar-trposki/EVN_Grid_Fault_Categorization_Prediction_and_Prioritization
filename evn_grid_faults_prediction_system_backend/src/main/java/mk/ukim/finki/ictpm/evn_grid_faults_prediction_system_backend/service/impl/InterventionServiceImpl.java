package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service.impl;

import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.InterventionCreateDto;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.InterventionResponseDto;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.Crew;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.FaultReport;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.Intervention;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.enums.FaultStatus;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.repository.CrewRepository;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.repository.FaultReportRepository;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.repository.InterventionRepository;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service.FaultWorkflowService;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service.InterventionService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InterventionServiceImpl implements InterventionService {

    private final InterventionRepository interventionRepo;
    private final FaultReportRepository faultRepo;
    private final CrewRepository crewRepo;
    private final FaultWorkflowService workflowService;

    public InterventionServiceImpl(InterventionRepository interventionRepo,
                                   FaultReportRepository faultRepo,
                                   CrewRepository crewRepo,
                                   FaultWorkflowService workflowService) {
        this.interventionRepo = interventionRepo;
        this.faultRepo = faultRepo;
        this.crewRepo = crewRepo;
        this.workflowService = workflowService;
    }

    @Override
    public InterventionResponseDto create(Long faultId, InterventionCreateDto dto) {
        FaultReport fault = faultRepo.findById(faultId)
                .orElseThrow(() -> new RuntimeException("Fault not found: " + faultId));
        Crew crew = crewRepo.findById(dto.getCrewId())
                .orElseThrow(() -> new RuntimeException("Crew not found: " + dto.getCrewId()));

        Intervention intervention = new Intervention();
        intervention.setDescription(dto.getDescription());
        intervention.setFaultReport(fault);
        intervention.setCrew(crew);
        intervention = interventionRepo.save(intervention);

        workflowService.changeStatus(fault, FaultStatus.IN_PROGRESS);

        return new InterventionResponseDto(
                intervention.getId(),
                intervention.getDescription(),
                crew.getName()
        );
    }

    @Override
    public List<InterventionResponseDto> getByFault(Long faultId) {
        return interventionRepo.findByFaultReportId(faultId).stream()
                .map(i -> new InterventionResponseDto(
                        i.getId(),
                        i.getDescription(),
                        i.getCrew().getName()
                ))
                .toList();
    }
}