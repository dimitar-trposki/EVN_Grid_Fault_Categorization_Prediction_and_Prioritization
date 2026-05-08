package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response;

import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.enums.RoleType;

public record UserSummaryResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        RoleType role
) {}
