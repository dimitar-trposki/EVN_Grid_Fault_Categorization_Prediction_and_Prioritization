package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.request;

public record CloseFaultRequest(
    String resolutionNotes,
    String rootCause
) {}
