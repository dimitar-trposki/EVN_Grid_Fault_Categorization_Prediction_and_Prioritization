package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service;

import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.WeatherDataResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface WeatherService {

    WeatherDataResponse fetchAndStore(Long locationId);

    int fetchAndStoreForAll();

    WeatherDataResponse getLatest(Long locationId);

    List<WeatherDataResponse> getHistory(Long locationId, LocalDateTime from, LocalDateTime to);

    /**
     * Safe lookup intended for Module 8 (Risk Prediction). Never throws — returns empty if no data or any error.
     */
    Optional<WeatherDataResponse> getLatestForRiskInput(Long locationId);
}
