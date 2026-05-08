package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response;

import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.enums.RoleType;

public record LoginResponse(
        String token,
        Long userId,
        String email,
        String firstName,
        String lastName,
        RoleType role
) {}
