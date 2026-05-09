package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.client.ai.AiClient;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.client.ai.dto.ClassificationInput;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.client.ai.dto.ClassificationResult;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.request.ManualClassificationOverrideRequest;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.ClassificationResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.exception.ResourceNotFoundException;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.helpers.FaultClassificationResultMapper;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.FaultClassificationResult;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.FaultReport;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.enums.FaultClassification;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.repository.FaultClassificationResultRepository;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.repository.FaultReportRepository;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service.FaultClassificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FaultClassificationServiceImpl implements FaultClassificationService {

    private final FaultReportRepository faultReportRepository;
    private final FaultClassificationResultRepository classificationRepository;
    private final AiClient aiClient;
    private final FaultClassificationResultMapper mapper;

    @Override
    @Transactional
    public ClassificationResponse classifyFault(Long faultReportId) {
        Optional<FaultClassificationResult> existing = classificationRepository.findByFaultReportId(faultReportId);
        if (existing.isPresent() && Boolean.TRUE.equals(existing.get().getNlpProcessed())) {
            return mapper.toResponse(existing.get());
        }
        return doClassify(faultReportId, existing.orElse(null));
    }

    @Override
    @Transactional
    public ClassificationResponse reclassify(Long faultReportId) {
        Optional<FaultClassificationResult> existing = classificationRepository.findByFaultReportId(faultReportId);
        return doClassify(faultReportId, existing.orElse(null));
    }

    @Override
    @Transactional(readOnly = true)
    public ClassificationResponse getByFault(Long faultReportId) {
        return classificationRepository.findByFaultReportId(faultReportId)
                .map(mapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No classification found for fault " + faultReportId));
    }

    @Override
    @Transactional
    public ClassificationResponse manualOverride(Long faultReportId, ManualClassificationOverrideRequest req) {
        FaultReport fault = faultReportRepository.findById(faultReportId)
                .orElseThrow(() -> new ResourceNotFoundException("FaultReport", faultReportId));

        FaultClassificationResult entity = classificationRepository.findByFaultReportId(faultReportId)
                .orElseGet(FaultClassificationResult::new);

        if (entity.getFaultReport() == null) {
            entity.setFaultReport(fault);
        }
        validateCategory(req.predictedFaultCategory());
        entity.setPredictedFaultCategory(req.predictedFaultCategory());
        entity.setPredictedSeverity(req.predictedSeverity());
        entity.setSafetyRisk(req.safetyRisk());
        entity.setExtractedKeywords(
                req.extractedKeywords() != null ? new ArrayList<>(req.extractedKeywords()) : new ArrayList<>()
        );
        entity.setClassificationConfidence(null);
        entity.setNlpProcessed(true);
        entity.setIsFallback(false);
        entity.setClassifiedAt(LocalDateTime.now());

        return mapper.toResponse(classificationRepository.save(entity));
    }

    // TODO: consider making this async (@Async + @EnableAsync) so the HTTP call to the AI service
    // does not block the caller or hold a DB connection open during the network wait.
    private ClassificationResponse doClassify(Long faultReportId, FaultClassificationResult existing) {
        FaultReport fault = faultReportRepository.findById(faultReportId)
                .orElseThrow(() -> new ResourceNotFoundException("FaultReport", faultReportId));

        ClassificationInput input = new ClassificationInput(
                fault.getId(),
                fault.getDescription(),
                buildLocationText(fault)
        );

        ClassificationResult aiResult = aiClient.classifyFault(input);
        validateCategory(aiResult.predictedCategory());

        FaultClassificationResult entity = existing != null ? existing : new FaultClassificationResult();
        entity.setFaultReport(fault);
        entity.setPredictedFaultCategory(aiResult.predictedCategory());
        entity.setPredictedSeverity(aiResult.predictedSeverity());
        entity.setClassificationConfidence(aiResult.confidence());
        entity.setExtractedKeywords(
                aiResult.keywords() != null ? new ArrayList<>(aiResult.keywords()) : new ArrayList<>()
        );
        entity.setSafetyRisk(aiResult.safetyRisk());
        entity.setNlpProcessed(!Boolean.TRUE.equals(aiResult.isFallback()));
        entity.setIsFallback(aiResult.isFallback());
        entity.setClassifiedAt(LocalDateTime.now());

        return mapper.toResponse(classificationRepository.save(entity));
    }

    private String buildLocationText(FaultReport fault) {
        return fault.getLocation().getAddress() + ", " + fault.getLocation().getRegion().getName();
    }

    private void validateCategory(String category) {
        if (category == null) return;
        try {
            FaultClassification.valueOf(category);
        } catch (IllegalArgumentException e) {
            log.warn("Unrecognized fault category '{}' from AI — storing raw string", category);
        }
    }
}
