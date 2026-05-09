package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response;

import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.enums.FaultStatus;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.enums.FaultType;

import java.time.LocalDateTime;
import java.util.List;

public record TrackFaultResponse(
        String trackingCode,
        String title,
        FaultStatus currentStatus,
        LocalDateTime reportedAt,
        FaultType faultType,
        String locationAddress,
        List<FaultStatusHistoryResponse> history
) {}
