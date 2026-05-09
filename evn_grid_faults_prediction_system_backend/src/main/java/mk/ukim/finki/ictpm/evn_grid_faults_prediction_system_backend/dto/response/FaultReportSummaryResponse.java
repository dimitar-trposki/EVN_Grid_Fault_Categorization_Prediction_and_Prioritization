package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response;

import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.enums.FaultClassification;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.enums.FaultPriority;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.enums.FaultStatus;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.enums.FaultType;

import java.time.LocalDateTime;

public record FaultReportSummaryResponse(
        Long id,
        String trackingCode,
        LocalDateTime reportedAt,
        String title,
        FaultType faultType,
        FaultPriority faultPriority,
        FaultClassification faultClassification,
        FaultStatus currentStatus,
        String locationAddress
) {}
