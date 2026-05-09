package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.helpers;

import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.FaultReportResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.FaultReportSummaryResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.FaultReport;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.FaultStatusHistory;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.enums.FaultStatus;
import org.springframework.stereotype.Component;

import java.util.Comparator;

@Component
public class FaultReportMapper {

    public FaultReportResponse toResponse(FaultReport f) {
        FaultStatus currentStatus = resolveCurrentStatus(f);
        return new FaultReportResponse(
                f.getId(),
                f.getTrackingCode(),
                f.getReportedAt(),
                f.getSourceType(),
                f.getTitle(),
                f.getDescription(),
                f.getFaultType(),
                f.getFaultPriority(),
                f.getFaultClassification(),
                f.getLocation().getId(),
                f.getLocation().getAddress(),
                f.getLocation().getRegion().getId(),
                f.getLocation().getRegion().getName(),
                f.getCustomer() != null ? f.getCustomer().getId() : null,
                currentStatus
        );
    }

    public FaultReportSummaryResponse toSummaryResponse(FaultReport f) {
        FaultStatus currentStatus = resolveCurrentStatus(f);
        return new FaultReportSummaryResponse(
                f.getId(),
                f.getTrackingCode(),
                f.getReportedAt(),
                f.getTitle(),
                f.getFaultType(),
                f.getFaultPriority(),
                f.getFaultClassification(),
                currentStatus,
                f.getLocation().getAddress()
        );
    }

    private FaultStatus resolveCurrentStatus(FaultReport f) {
        if (f.getFaultStatusHistories() == null || f.getFaultStatusHistories().isEmpty()) {
            return null;
        }
        return f.getFaultStatusHistories().stream()
                .max(Comparator.comparing(FaultStatusHistory::getChangedAt))
                .map(FaultStatusHistory::getFaultStatus)
                .orElse(null);
    }
}
