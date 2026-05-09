package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service;

import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.WeatherDataResponse;

import java.time.LocalDateTime;
import java.util.List;

public interface WeatherService {
    WeatherDataResponse fetchAndStore(Long locationId);
    WeatherDataResponse getLatest(Long locationId);
    List<WeatherDataResponse> getHistory(Long locationId, LocalDateTime from, LocalDateTime to);
}
