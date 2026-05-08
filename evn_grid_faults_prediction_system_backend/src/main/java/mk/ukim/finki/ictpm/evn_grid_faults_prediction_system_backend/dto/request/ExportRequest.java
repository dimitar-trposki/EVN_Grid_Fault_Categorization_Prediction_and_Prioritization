package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.request;

import java.util.Map;

public record ExportRequest(
        String type,
        String format,
        Map<String, String> filters
) {}
