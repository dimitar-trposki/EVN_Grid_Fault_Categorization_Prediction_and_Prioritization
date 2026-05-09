package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto;

import lombok.Getter;
import lombok.Setter;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.enums.FaultType;

@Deprecated
@Getter
@Setter
public class CreateFaultReportDto {
    private String title;
    private String description;
    private Long locationId;
    private Long customerId;
    private FaultType faultType;
}