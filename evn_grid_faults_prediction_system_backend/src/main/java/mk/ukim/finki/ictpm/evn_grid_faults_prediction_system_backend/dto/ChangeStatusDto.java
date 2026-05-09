package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto;

import lombok.Getter;
import lombok.Setter;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.enums.FaultStatus;

@Deprecated
@Getter
@Setter
public class ChangeStatusDto {
    private FaultStatus status;
}