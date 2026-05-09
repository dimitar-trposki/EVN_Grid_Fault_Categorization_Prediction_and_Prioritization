package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.enums.FaultStatus;

import java.time.LocalDateTime;

@Deprecated
@Getter
@Setter
@AllArgsConstructor
public class StatusHistoryDto {
    private FaultStatus status;
    private LocalDateTime changedAt;
}