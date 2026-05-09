package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AddCrewMemberRequest(
    @NotNull(message = "User ID is required")
    Long userId,

    @Size(max = 80, message = "Position must not exceed 80 characters")
    String position
) {}
