package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service.impl;

import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.CreateFaultReportDto;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.FaultReportResponseDto;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.Customer;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.FaultReport;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.Location;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.enums.FaultClassification;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.enums.FaultPriority;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.enums.FaultStatus;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.repository.CustomerRepository;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.repository.FaultReportRepository;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.repository.LocationRepository;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service.FaultClassificationService;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service.FaultReportService;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service.FaultWorkflowService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.FaultStatusHistory;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
public class FaultReportServiceImpl implements FaultReportService {

    private final FaultReportRepository faultRepo;
    private final LocationRepository locationRepo;
    private final CustomerRepository customerRepo;
    private final FaultWorkflowService workflowService;
    private final FaultClassificationService faultClassificationService;

    public FaultReportServiceImpl(
            FaultReportRepository faultRepo,
            LocationRepository locationRepo,
            CustomerRepository customerRepo,
            FaultWorkflowService workflowService,
            FaultClassificationService faultClassificationService
    ) {
        this.faultRepo = faultRepo;
        this.locationRepo = locationRepo;
        this.customerRepo = customerRepo;
        this.workflowService = workflowService;
        this.faultClassificationService = faultClassificationService;
    }

    @Override
    public FaultReportResponseDto createFault(CreateFaultReportDto dto) {

        Location location = locationRepo.findById(dto.getLocationId())
                .orElseThrow(() -> new RuntimeException("Not found"));

        Customer customer = customerRepo.findById(dto.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Not found"));

        FaultReport fault = new FaultReport();
        fault.setTitle(dto.getTitle());
        fault.setDescription(dto.getDescription());
        fault.setLocation(location);
        fault.setCustomer(customer);
        fault.setFaultType(dto.getFaultType());
        fault.setFaultPriority(FaultPriority.LOW);
        fault.setFaultClassification(FaultClassification.OTHER);

        fault = faultRepo.save(fault);

        // TODO: make async so the AI HTTP call does not block the customer's create-fault request
        try {
            faultClassificationService.classifyFault(fault.getId());
        } catch (Exception e) {
            log.warn("Auto-classification failed for faultId={}: {}", fault.getId(), e.getMessage());
        }

        workflowService.changeStatus(fault, FaultStatus.REPORTED);

        return map(fault);
    }

    @Override
    public List<FaultReportResponseDto> getAll() {
        return faultRepo.findAll().stream().map(this::map).toList();
    }

    @Override
    public FaultReportResponseDto getById(Long id) {
        return map(faultRepo.findById(id).orElseThrow(() -> new RuntimeException("Not found")));
    }

    @Override
    public FaultReportResponseDto changeStatus(Long id, FaultStatus status) {
        FaultReport fault = faultRepo.findById(id).orElseThrow(() -> new RuntimeException("Not found"));

        workflowService.changeStatus(fault, status);

        return map(fault);
    }

    private FaultReportResponseDto map(FaultReport f) {
        return new FaultReportResponseDto(
                f.getId(),
                f.getTitle(),
                f.getDescription(),
                f.getFaultStatusHistories().stream()
                        .max(Comparator.comparing(FaultStatusHistory::getChangedAt))
                        .map(FaultStatusHistory::getFaultStatus)
                        .orElse(null),
                f.getFaultPriority(),
                f.getLocation().getId().toString()
        );
    }
}