package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.enums.RoleType;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDto {

    private String token;
    private String email;
    private String firstName;
    private String lastName;
    private RoleType role;
}
