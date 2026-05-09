package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.client.ai.dto;

public record ClassificationInput(
        Long faultId,
        String description,
        String locationText
) {}
