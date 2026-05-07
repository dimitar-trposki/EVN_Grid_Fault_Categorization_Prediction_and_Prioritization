package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @NotBlank(message = "First name is required")
        @Size(max = 80, message = "First name must not exceed 80 characters")
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(max = 80, message = "Last name must not exceed 80 characters")
        String lastName,

        @Size(max = 150, message = "Phone must not exceed 150 characters")
        String phone
) {}
