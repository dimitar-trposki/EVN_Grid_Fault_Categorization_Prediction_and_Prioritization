package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.client.weather.WeatherFetchResult;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.client.weather.WeatherProvider;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.WeatherDataResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.exception.ExternalApiException;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.exception.ResourceNotFoundException;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.Location;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.WeatherData;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.repository.LocationRepository;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.repository.WeatherDataRepository;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service.WeatherService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherServiceImpl implements WeatherService {

    private final WeatherProvider weatherProvider;
    private final LocationRepository locationRepository;
    private final WeatherDataRepository weatherDataRepository;

    @Override
    @Transactional
    public WeatherDataResponse fetchAndStore(Long locationId) {
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new ResourceNotFoundException("Location", locationId));

        WeatherFetchResult result = doFetch(location);
        return map(weatherDataRepository.save(buildEntity(location, result)));
    }

    @Override
    public int fetchAndStoreForAll() {
        List<Location> locations = locationRepository.findAll();
        int successCount = 0;
        for (Location location : locations) {
            try {
                WeatherFetchResult result = doFetch(location);
                weatherDataRepository.save(buildEntity(location, result));
                successCount++;
                if (locations.size() > 10) {
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Weather batch refresh interrupted after {} locations", successCount);
                break;
            } catch (Exception e) {
                log.warn("Batch weather fetch skipped location {}: {}", location.getId(), e.getMessage());
            }
        }
        log.info("Weather batch refresh complete: {}/{} locations updated", successCount, locations.size());
        return successCount;
    }

    @Override
    public WeatherDataResponse getLatest(Long locationId) {
        if (!locationRepository.existsById(locationId)) {
            throw new ResourceNotFoundException("Location", locationId);
        }
        return weatherDataRepository.findTopByLocationIdOrderByRecordedAtDesc(locationId)
                .map(this::map)
                .orElseThrow(() -> new ResourceNotFoundException("No weather data found for location " + locationId));
    }

    @Override
    public List<WeatherDataResponse> getHistory(Long locationId, LocalDateTime from, LocalDateTime to) {
        if (!locationRepository.existsById(locationId)) {
            throw new ResourceNotFoundException("Location", locationId);
        }
        return weatherDataRepository.findByLocationIdAndRecordedAtBetween(locationId, from, to)
                .stream()
                .map(this::map)
                .toList();
    }

    @Override
    public Optional<WeatherDataResponse> getLatestForRiskInput(Long locationId) {
        try {
            return weatherDataRepository.findTopByLocationIdOrderByRecordedAtDesc(locationId)
                    .map(this::map);
        } catch (Exception e) {
            log.warn("getLatestForRiskInput failed silently for location {}: {}", locationId, e.getMessage());
            return Optional.empty();
        }
    }

    private WeatherFetchResult doFetch(Location location) {
        try {
            return weatherProvider.fetchCurrent(location.getLatitude(), location.getLongitude());
        } catch (Exception e) {
            log.warn("Weather API call failed for location {}: {}", location.getId(), e.getMessage());
            throw new ExternalApiException(
                    "Failed to fetch weather for location " + location.getId() + ": " + e.getMessage());
        }
    }

    private WeatherData buildEntity(Location location, WeatherFetchResult result) {
        WeatherData data = new WeatherData();
        data.setLocation(location);
        data.setRecordedAt(LocalDateTime.now());
        data.setTemperature(result.temperature());
        data.setWindSpeed(result.windSpeed());
        data.setHumidity(result.humidity());
        data.setPrecipitation(result.precipitation());
        data.setWeatherCondition(result.weatherCondition());
        data.setSourceApi(result.sourceApi());
        return data;
    }

    private WeatherDataResponse map(WeatherData w) {
        return new WeatherDataResponse(
                w.getId(),
                w.getLocation().getId(),
                w.getRecordedAt(),
                w.getTemperature(),
                w.getWindSpeed(),
                w.getHumidity(),
                w.getPrecipitation(),
                w.getWeatherCondition(),
                w.getSourceApi()
        );
    }
}
