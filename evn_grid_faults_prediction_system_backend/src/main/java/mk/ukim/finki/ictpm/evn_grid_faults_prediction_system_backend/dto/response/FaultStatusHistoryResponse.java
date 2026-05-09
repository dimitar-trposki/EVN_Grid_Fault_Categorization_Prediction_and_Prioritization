package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response;

import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.enums.FaultStatus;

import java.time.LocalDateTime;

public record FaultStatusHistoryResponse(
        Long id,
        FaultStatus faultStatus,
        LocalDateTime changedAt,
        String note,
        Boolean customerVisible,
        String changedByName
) {}
