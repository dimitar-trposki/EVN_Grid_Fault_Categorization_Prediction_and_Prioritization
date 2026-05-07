package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response;

public record LocationResponse(
        Long id,
        Double latitude,
        Double longitude,
        String address,
        Long regionId,
        String regionName
) {}