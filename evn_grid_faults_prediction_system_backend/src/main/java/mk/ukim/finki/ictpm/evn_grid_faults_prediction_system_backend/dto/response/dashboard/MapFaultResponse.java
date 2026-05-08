package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.dashboard;

import java.time.LocalDateTime;

public record MapFaultResponse(
        Long id,
        double latitude,
        double longitude,
        String status,
        String priorityLevel,
        String faultTypeName,
        LocalDateTime reportedAt
) {}
