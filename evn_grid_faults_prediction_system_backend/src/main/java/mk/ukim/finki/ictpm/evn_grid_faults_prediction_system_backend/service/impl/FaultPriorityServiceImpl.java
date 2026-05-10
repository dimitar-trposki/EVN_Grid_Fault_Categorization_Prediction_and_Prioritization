package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.client.ai.AiClient;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.client.ai.dto.PriorityCalculationInput;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.client.ai.dto.PriorityCalculationResult;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.request.ManualPriorityOverrideRequest;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.FaultPriorityResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.WeatherDataResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.exception.ResourceNotFoundException;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.helpers.FaultPriorityMapper;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.FaultClassificationResult;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.FaultPriorityRecord;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.FaultReport;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.enums.FaultPriority;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.repository.FaultClassificationResultRepository;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.repository.FaultPriorityRepository;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.repository.FaultReportRepository;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service.AuditLogService;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service.FaultPriorityService;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service.WeatherService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FaultPriorityServiceImpl implements FaultPriorityService {

    private final FaultReportRepository faultReportRepository;
    private final FaultClassificationResultRepository classificationRepository;
    private final FaultPriorityRepository priorityRepository;
    private final WeatherService weatherService;
    private final AiClient aiClient;
    private final FaultPriorityMapper mapper;
    private final AuditLogService auditLogService;

    @Override
    @Transactional
    public FaultPriorityResponse calculatePriority(Long faultReportId) {
        Optional<FaultPriorityRecord> existing = priorityRepository.findByFaultReportId(faultReportId);
        // skip re-calculation if a non-fallback record already exists
        if (existing.isPresent() && !Boolean.TRUE.equals(existing.get().getIsFallback())) {
            return mapper.toResponse(existing.get());
        }
        return doCalculate(faultReportId, existing.orElse(null));
    }

    @Override
    @Transactional
    public FaultPriorityResponse recalculate(Long faultReportId) {
        Optional<FaultPriorityRecord> existing = priorityRepository.findByFaultReportId(faultReportId);
        return doCalculate(faultReportId, existing.orElse(null));
    }

    @Override
    @Transactional(readOnly = true)
    public FaultPriorityResponse getByFault(Long faultReportId) {
        return priorityRepository.findByFaultReportId(faultReportId)
                .map(mapper::toResponse)
                .orElse(null);
    }

    @Override
    @Transactional
    public FaultPriorityResponse manualOverride(Long faultReportId, ManualPriorityOverrideRequest req) {
        FaultReport fault = faultReportRepository.findById(faultReportId)
                .orElseThrow(() -> new ResourceNotFoundException("FaultReport", faultReportId));

        FaultPriorityRecord entity = priorityRepository.findByFaultReportId(faultReportId)
                .orElseGet(FaultPriorityRecord::new);

        FaultPriority oldLevel = entity.getPriorityLevel();

        if (entity.getFaultReport() == null) {
            entity.setFaultReport(fault);
        }
        entity.setPriorityLevel(req.priorityLevel());
        entity.setExplanation(req.explanation());
        entity.setCalculatedAt(LocalDateTime.now());
        entity.setCalculationSource("MANUAL");
        entity.setIsFallback(false);
        if (entity.getPriorityScore() == null) {
            entity.setPriorityScore(scoreFromLevel(req.priorityLevel()));
        }

        fault.setFaultPriority(req.priorityLevel());
        faultReportRepository.save(fault);

        FaultPriorityResponse result = mapper.toResponse(priorityRepository.save(entity));

        try {
            auditLogService.log("FaultPriority", entity.getId(), "PRIORITY_OVERRIDE",
                    oldLevel != null ? oldLevel.name() : null, req.priorityLevel().name());
        } catch (Exception e) {
            log.warn("Audit log failed for PRIORITY_OVERRIDE faultId={}: {}", faultReportId, e.getMessage());
        }

        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<FaultPriorityResponse> getTopPriorityFaults(int limit) {
        return priorityRepository.findTopActiveFaultPriorities(limit).stream()
                .map(mapper::toResponse)
                .toList();
    }

    private FaultPriorityResponse doCalculate(Long faultReportId, FaultPriorityRecord existing) {
        FaultReport fault = faultReportRepository.findById(faultReportId)
                .orElseThrow(() -> new ResourceNotFoundException("FaultReport", faultReportId));

        Optional<FaultClassificationResult> classification =
                classificationRepository.findByFaultReportId(faultReportId);

        Optional<WeatherDataResponse> weather =
                weatherService.getLatestForRiskInput(fault.getLocation().getId());

        // recurrence heuristic: more than one fault at this location in the past 30 days
        long recentFaultCount = faultReportRepository.countByLocationIdReportedAfter(
                fault.getLocation().getId(), LocalDateTime.now().minusDays(30));
        boolean isRecurring = recentFaultCount > 1;

        String faultCategory = classification
                .map(FaultClassificationResult::getPredictedFaultCategory)
                .filter(c -> c != null && !c.isBlank())
                .orElse(fault.getFaultType().name());

        String severity = classification
                .map(FaultClassificationResult::getPredictedSeverity)
                .filter(s -> s != null && !s.isBlank())
                .orElse("MEDIUM");

        Boolean safetyRisk = classification
                .map(FaultClassificationResult::getSafetyRisk)
                .orElse(false);

        String weatherCond = weather
                .map(w -> w.weatherCondition() != null ? w.weatherCondition().name() : null)
                .orElse(null);

        PriorityCalculationInput input = new PriorityCalculationInput(
                fault.getId(),
                faultCategory,
                severity,
                safetyRisk,
                0,                                          // affectedUsersEstimate: no field on FaultReport
                fault.getLocation().getRegion().getName(),  // locationCriticality: region name as label
                weatherCond,
                isRecurring
        );

        PriorityCalculationResult aiResult = aiClient.calculatePriority(input);

        FaultPriorityRecord entity = existing != null ? existing : new FaultPriorityRecord();
        entity.setFaultReport(fault);
        entity.setPriorityScore(aiResult.priorityScore());
        entity.setExplanation(aiResult.explanation());
        entity.setCalculatedAt(LocalDateTime.now());
        entity.setIsFallback(aiResult.isFallback());

        FaultPriority level = parsePriorityLevel(aiResult.priorityLevel());
        entity.setPriorityLevel(level);
        entity.setCalculationSource(Boolean.TRUE.equals(aiResult.isFallback()) ? "FALLBACK" : "AI");

        fault.setFaultPriority(level);
        faultReportRepository.save(fault);

        return mapper.toResponse(priorityRepository.save(entity));
    }

    private FaultPriority parsePriorityLevel(String level) {
        if (level == null) return FaultPriority.MEDIUM;
        try {
            return FaultPriority.valueOf(level.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Unrecognized priority level '{}' from AI — defaulting to MEDIUM", level);
            return FaultPriority.MEDIUM;
        }
    }

    private double scoreFromLevel(FaultPriority level) {
        return switch (level) {
            case CRITICAL -> 90.0;
            case HIGH -> 70.0;
            case MEDIUM -> 50.0;
            case LOW -> 25.0;
        };
    }
}
