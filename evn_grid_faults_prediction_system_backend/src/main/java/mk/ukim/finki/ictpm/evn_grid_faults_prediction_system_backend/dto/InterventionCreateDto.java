package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto;

import lombok.Getter;
import lombok.Setter;

/** @deprecated Use StartInterventionRequest */
@Deprecated
@Getter
@Setter
public class InterventionCreateDto {
    private String description;
    private Long crewId;
}