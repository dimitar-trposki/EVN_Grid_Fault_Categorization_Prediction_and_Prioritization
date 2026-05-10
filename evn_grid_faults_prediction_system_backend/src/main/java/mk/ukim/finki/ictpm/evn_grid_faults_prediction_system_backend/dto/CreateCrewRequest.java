package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.enums.CrewStatus;

public record CreateCrewRequest(
    @NotBlank(message = "Crew name is required")
    @Size(max = 80, message = "Crew name must not exceed 80 characters")
    String name,

    @Size(max = 30, message = "Crew code must not exceed 30 characters")
    String crewCode,

    CrewStatus status,

    Double currentLatitude,

    Double currentLongitude
) {}
