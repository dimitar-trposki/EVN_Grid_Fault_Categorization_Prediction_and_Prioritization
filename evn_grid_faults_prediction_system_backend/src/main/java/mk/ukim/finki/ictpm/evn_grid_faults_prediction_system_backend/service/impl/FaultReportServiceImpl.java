package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service.impl;

import lombok.extern.slf4j.Slf4j;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.CreateFaultReportDto;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.FaultReportResponseDto;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.request.CreateFaultReportRequest;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.request.FaultStatusUpdateRequest;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.request.OperatorCreateFaultRequest;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.request.UpdateFaultReportRequest;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.FaultReportResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.FaultReportSummaryResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.FaultStatusHistoryResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.TrackFaultResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.exception.ResourceNotFoundException;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.helpers.FaultReportMapper;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.helpers.FaultStatusHistoryMapper;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.Customer;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.FaultReport;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.FaultStatusHistory;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.Location;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.User;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.enums.FaultClassification;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.enums.FaultPriority;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.enums.FaultSourceType;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.enums.FaultStatus;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.enums.FaultType;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.enums.RoleType;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.repository.CustomerRepository;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.repository.FaultReportRepository;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.repository.FaultStatusHistoryRepository;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.repository.LocationRepository;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.repository.UserRepository;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service.AuditLogService;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service.FaultClassificationService;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service.FaultPriorityService;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service.FaultReportService;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service.FaultWorkflowService;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service.NotificationService;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.specification.FaultSpecification;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.util.TrackingCodeGenerator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@Transactional
public class FaultReportServiceImpl implements FaultReportService {

    private final FaultReportRepository faultRepo;
    private final LocationRepository locationRepo;
    private final CustomerRepository customerRepo;
    private final UserRepository userRepo;
    private final FaultStatusHistoryRepository historyRepo;
    private final FaultWorkflowService workflowService;
    private final FaultClassificationService faultClassificationService;
    private final FaultPriorityService faultPriorityService;
    private final FaultReportMapper faultReportMapper;
    private final FaultStatusHistoryMapper historyMapper;
    private final TrackingCodeGenerator trackingCodeGenerator;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;

    public FaultReportServiceImpl(
            FaultReportRepository faultRepo,
            LocationRepository locationRepo,
            CustomerRepository customerRepo,
            UserRepository userRepo,
            FaultStatusHistoryRepository historyRepo,
            FaultWorkflowService workflowService,
            FaultClassificationService faultClassificationService,
            FaultPriorityService faultPriorityService,
            FaultReportMapper faultReportMapper,
            FaultStatusHistoryMapper historyMapper,
            TrackingCodeGenerator trackingCodeGenerator,
            NotificationService notificationService,
            AuditLogService auditLogService
    ) {
        this.faultRepo = faultRepo;
        this.locationRepo = locationRepo;
        this.customerRepo = customerRepo;
        this.userRepo = userRepo;
        this.historyRepo = historyRepo;
        this.workflowService = workflowService;
        this.faultClassificationService = faultClassificationService;
        this.faultPriorityService = faultPriorityService;
        this.faultReportMapper = faultReportMapper;
        this.historyMapper = historyMapper;
        this.trackingCodeGenerator = trackingCodeGenerator;
        this.notificationService = notificationService;
        this.auditLogService = auditLogService;
    }

    // --- Legacy deprecated methods ---

    @Override
    @Deprecated
    public FaultReportResponseDto createFault(CreateFaultReportDto dto) {
        Location location = locationRepo.findById(dto.getLocationId())
                .orElseThrow(() -> new ResourceNotFoundException("Location", dto.getLocationId()));
        Customer customer = customerRepo.findById(dto.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer", dto.getCustomerId()));
        FaultReport fault = buildFault(dto.getTitle(), dto.getDescription(), location, customer,
                dto.getFaultType(), FaultPriority.LOW, FaultClassification.OTHER, FaultSourceType.CUSTOMER_PORTAL);
        fault = faultRepo.save(fault);
        runAiLifecycle(fault);
        workflowService.changeStatus(fault, FaultStatus.REPORTED);
        return mapLegacy(fault);
    }

    @Override
    @Deprecated
    @Transactional(readOnly = true)
    public List<FaultReportResponseDto> getAll() {
        return faultRepo.findAll().stream().map(this::mapLegacy).toList();
    }

    @Override
    @Deprecated
    @Transactional(readOnly = true)
    public FaultReportResponseDto getById(Long id) {
        return mapLegacy(faultRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FaultReport", id)));
    }

    @Override
    @Deprecated
    public FaultReportResponseDto changeStatus(Long id, FaultStatus status) {
        FaultReport fault = faultRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FaultReport", id));
        workflowService.changeStatus(fault, status);
        return mapLegacy(fault);
    }

    // --- New methods ---

    @Override
    public FaultReportResponse createByCustomer(CreateFaultReportRequest dto, String callerEmail) {
        Location location = locationRepo.findById(dto.locationId())
                .orElseThrow(() -> new ResourceNotFoundException("Location", dto.locationId()));
        Customer customer = customerRepo.findByUserEmail(callerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Customer for email: " + callerEmail));
        FaultReport fault = buildFault(dto.title(), dto.description(), location, customer,
                dto.faultType(), FaultPriority.LOW, FaultClassification.OTHER, FaultSourceType.CUSTOMER_PORTAL);
        fault = faultRepo.save(fault);
        try {
            auditLogService.log("FaultReport", fault.getId(), "CREATE", null,
                    "trackingCode=" + fault.getTrackingCode());
        } catch (Exception e) {
            log.warn("Audit log failed for fault CREATE id={}: {}", fault.getId(), e.getMessage());
        }
        runAiLifecycle(fault);
        workflowService.changeStatus(fault, FaultStatus.REPORTED);
        return faultReportMapper.toResponse(fault);
    }

    @Override
    public FaultReportResponse createByOperator(OperatorCreateFaultRequest dto, String callerEmail) {
        Location location = locationRepo.findById(dto.locationId())
                .orElseThrow(() -> new ResourceNotFoundException("Location", dto.locationId()));
        Customer customer = null;
        if (dto.customerId() != null) {
            customer = customerRepo.findById(dto.customerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Customer", dto.customerId()));
        }
        FaultReport fault = buildFault(dto.title(), dto.description(), location, customer,
                dto.faultType(), FaultPriority.LOW, FaultClassification.OTHER, FaultSourceType.OPERATOR_CALL_CENTER);
        fault = faultRepo.save(fault);
        try {
            auditLogService.log("FaultReport", fault.getId(), "CREATE", null,
                    "trackingCode=" + fault.getTrackingCode());
        } catch (Exception e) {
            log.warn("Audit log failed for fault CREATE id={}: {}", fault.getId(), e.getMessage());
        }
        runAiLifecycle(fault);
        workflowService.changeStatus(fault, FaultStatus.REPORTED);
        return faultReportMapper.toResponse(fault);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FaultReportSummaryResponse> getFiltered(
            FaultType faultType, FaultPriority faultPriority, FaultClassification faultClassification,
            FaultStatus status, Long locationId, Long regionId, Long customerId,
            LocalDateTime from, LocalDateTime to, Pageable pageable) {
        Specification<FaultReport> spec = Specification.where((root, query, cb) -> cb.conjunction());
        spec = spec.and(FaultSpecification.hasFaultType(faultType));
        spec = spec.and(FaultSpecification.hasFaultPriority(faultPriority));
        spec = spec.and(FaultSpecification.hasFaultClassification(faultClassification));
        spec = spec.and(FaultSpecification.hasCurrentStatus(status));
        spec = spec.and(FaultSpecification.hasLocationId(locationId));
        spec = spec.and(FaultSpecification.hasRegionId(regionId));
        spec = spec.and(FaultSpecification.hasCustomerId(customerId));
        spec = spec.and(FaultSpecification.reportedAfter(from));
        spec = spec.and(FaultSpecification.reportedBefore(to));
        return faultRepo.findAll(spec, pageable).map(faultReportMapper::toSummaryResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FaultReportSummaryResponse> getMyFaults(String callerEmail) {
        Customer customer = customerRepo.findByUserEmail(callerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Customer for email: " + callerEmail));
        return faultRepo.findAll(FaultSpecification.hasCustomerId(customer.getId()))
                .stream()
                .map(faultReportMapper::toSummaryResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public FaultReportResponse getFaultById(Long id) {
        return faultReportMapper.toResponse(faultRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FaultReport", id)));
    }

    @Override
    @Transactional(readOnly = true)
    public TrackFaultResponse getByTrackingCode(String trackingCode) {
        FaultReport fault = faultRepo.findByTrackingCode(trackingCode)
                .orElseThrow(() -> new ResourceNotFoundException("FaultReport with tracking code: " + trackingCode));
        List<FaultStatusHistoryResponse> history = historyRepo
                .findByFaultReportIdOrderByChangedAtDesc(fault.getId())
                .stream()
                .filter(h -> Boolean.TRUE.equals(h.getCustomerVisible()))
                .map(historyMapper::toResponse)
                .toList();
        FaultStatus currentStatus = history.isEmpty() ? null : history.get(0).faultStatus();
        return new TrackFaultResponse(
                fault.getTrackingCode(),
                fault.getTitle(),
                currentStatus,
                fault.getReportedAt(),
                fault.getFaultType(),
                fault.getLocation().getAddress(),
                history
        );
    }

    @Override
    public FaultReportResponse updateFault(Long id, UpdateFaultReportRequest dto) {
        FaultReport fault = faultRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FaultReport", id));
        Location location = locationRepo.findById(dto.locationId())
                .orElseThrow(() -> new ResourceNotFoundException("Location", dto.locationId()));
        fault.setTitle(dto.title());
        fault.setDescription(dto.description());
        fault.setLocation(location);
        fault.setFaultType(dto.faultType());
        if (dto.faultPriority() != null) fault.setFaultPriority(dto.faultPriority());
        if (dto.faultClassification() != null) fault.setFaultClassification(dto.faultClassification());
        return faultReportMapper.toResponse(faultRepo.save(fault));
    }

    @Override
    public void updateStatus(Long id, FaultStatusUpdateRequest dto, String callerEmail) {
        FaultReport fault = faultRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FaultReport", id));
        User user = userRepo.findByEmail(callerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User for email: " + callerEmail));
        FaultStatus oldStatus = historyRepo.findByFaultReportIdOrderByChangedAtDesc(id)
                .stream().findFirst().map(FaultStatusHistory::getFaultStatus).orElse(null);
        boolean customerVisible = dto.customerVisible() != null ? dto.customerVisible() : true;
        if (user.getUserRole() == RoleType.CUSTOMER) {
            workflowService.changeStatus(fault, dto.status(), null, user.getCustomer(), dto.note(), customerVisible);
        } else {
            workflowService.changeStatus(fault, dto.status(), user, null, dto.note(), customerVisible);
        }
        try {
            auditLogService.log("FaultReport", id, "STATUS_CHANGE",
                    oldStatus != null ? oldStatus.name() : null, dto.status().name());
        } catch (Exception e) {
            log.warn("Audit log failed for STATUS_CHANGE faultId={}: {}", id, e.getMessage());
        }
        if (fault.getCustomer() != null) {
            try {
                String msg = "Fault " + fault.getTrackingCode()
                        + " status changed from " + (oldStatus != null ? oldStatus.name() : "N/A")
                        + " to " + dto.status().name();
                notificationService.sendToCustomer(fault.getCustomer().getId(),
                        "Fault status updated", msg, "STATUS_CHANGE");
            } catch (Exception e) {
                log.warn("Notification failed for STATUS_CHANGE faultId={}: {}", id, e.getMessage());
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<FaultStatusHistoryResponse> getStatusHistory(Long id) {
        if (!faultRepo.existsById(id)) {
            throw new ResourceNotFoundException("FaultReport", id);
        }
        return historyRepo.findByFaultReportIdOrderByChangedAtDesc(id)
                .stream()
                .map(historyMapper::toResponse)
                .toList();
    }

    @Override
    public void delete(Long id) {
        if (!faultRepo.existsById(id)) {
            throw new ResourceNotFoundException("FaultReport", id);
        }
        faultRepo.deleteById(id);
    }

    // --- Private helpers ---

    private FaultReport buildFault(String title, String description, Location location,
                                    Customer customer, FaultType faultType,
                                    FaultPriority priority, FaultClassification classification,
                                    FaultSourceType sourceType) {
        FaultReport fault = new FaultReport();
        fault.setTitle(title);
        fault.setDescription(description);
        fault.setLocation(location);
        fault.setCustomer(customer);
        fault.setFaultType(faultType);
        fault.setFaultPriority(priority);
        fault.setFaultClassification(classification);
        fault.setSourceType(sourceType);
        fault.setTrackingCode(trackingCodeGenerator.generate());
        return fault;
    }

    private void runAiLifecycle(FaultReport fault) {
        try {
            faultClassificationService.classifyFault(fault.getId());
        } catch (Exception e) {
            log.warn("Auto-classification failed for faultId={}: {}", fault.getId(), e.getMessage());
        }
        try {
            faultPriorityService.calculatePriority(fault.getId());
        } catch (Exception e) {
            log.warn("Auto-priority calculation failed for faultId={}: {}", fault.getId(), e.getMessage());
        }
    }

    private FaultReportResponseDto mapLegacy(FaultReport f) {
        FaultStatus status = null;
        if (f.getFaultStatusHistories() != null && !f.getFaultStatusHistories().isEmpty()) {
            status = f.getFaultStatusHistories().stream()
                    .max(Comparator.comparing(FaultStatusHistory::getChangedAt))
                    .map(FaultStatusHistory::getFaultStatus)
                    .orElse(null);
        }
        return new FaultReportResponseDto(
                f.getId(),
                f.getTitle(),
                f.getDescription(),
                status,
                f.getFaultPriority(),
                f.getLocation().getId().toString()
        );
    }
}
