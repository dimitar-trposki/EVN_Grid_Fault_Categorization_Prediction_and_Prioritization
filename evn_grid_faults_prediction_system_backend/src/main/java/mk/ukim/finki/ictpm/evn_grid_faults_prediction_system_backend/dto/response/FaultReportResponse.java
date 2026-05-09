package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response;

import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.enums.FaultClassification;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.enums.FaultPriority;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.enums.FaultSourceType;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.enums.FaultStatus;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.enums.FaultType;

import java.time.LocalDateTime;

public record FaultReportResponse(
        Long id,
        String trackingCode,
        LocalDateTime reportedAt,
        FaultSourceType sourceType,
        String title,
        String description,
        FaultType faultType,
        FaultPriority faultPriority,
        FaultClassification faultClassification,
        Long locationId,
        String locationAddress,
        Long regionId,
        String regionName,
        Long customerId,
        FaultStatus currentStatus
) {}
