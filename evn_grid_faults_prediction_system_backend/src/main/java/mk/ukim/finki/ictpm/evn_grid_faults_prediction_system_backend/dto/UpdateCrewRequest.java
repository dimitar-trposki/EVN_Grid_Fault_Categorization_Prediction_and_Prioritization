package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto;

import jakarta.validation.constraints.Size;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.enums.CrewStatus;

public record UpdateCrewRequest(
    @Size(max = 80, message = "Crew name must not exceed 80 characters")
    String name,

    CrewStatus status,

    Double currentLatitude,

    Double currentLongitude
) {}
