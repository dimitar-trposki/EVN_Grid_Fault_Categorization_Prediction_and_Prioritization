package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.enums.FaultPriority;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.enums.FaultStatus;

@Deprecated
@Getter
@Setter
@AllArgsConstructor
public class FaultReportResponseDto {
    private Long id;
    private String title;
    private String description;
    private FaultStatus status;
    private FaultPriority priority;
    private String location;
}
