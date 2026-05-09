package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response;

import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.enums.FaultPriority;

import java.time.LocalDateTime;

public record FaultPriorityResponse(
        Long id,
        Long faultReportId,
        FaultPriority priorityLevel,
        Double priorityScore,
        String explanation,
        LocalDateTime calculatedAt,
        String calculationSource,
        Boolean isFallback
) {}
