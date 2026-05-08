package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.enums.EquipmentType;

public record EquipmentRequest(
        @NotBlank(message = "Equipment name is required")
        @Size(max = 80, message = "Name must not exceed 80 characters")
        String name,

        @NotNull(message = "Equipment type is required")
        EquipmentType equipmentType,

        @NotNull(message = "Location ID is required")
        Long locationId
) {}
