package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/** @deprecated Use InterventionResponse */
@Deprecated
@Getter
@Setter
@AllArgsConstructor
public class InterventionResponseDto {
    private Long id;
    private String description;
    private String crewName;
}