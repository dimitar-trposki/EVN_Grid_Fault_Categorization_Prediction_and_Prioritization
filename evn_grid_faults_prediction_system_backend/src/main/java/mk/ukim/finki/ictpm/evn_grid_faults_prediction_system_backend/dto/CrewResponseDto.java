package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;

/** @deprecated Use {@link CrewResponse} */
@Deprecated
@Getter
@Setter
@AllArgsConstructor
public class CrewResponseDto {
    private Long id;
    private String name;
    private int memberCount;
}