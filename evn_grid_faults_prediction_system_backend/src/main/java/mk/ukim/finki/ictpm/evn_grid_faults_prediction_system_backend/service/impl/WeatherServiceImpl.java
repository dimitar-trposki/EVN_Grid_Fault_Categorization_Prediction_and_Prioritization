package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service.impl;

import lombok.RequiredArgsConstructor;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.client.WeatherClient;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.client.WeatherClient.ExternalWeatherResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.WeatherDataResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.exception.ResourceNotFoundException;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.mapper.WeatherDataMapper;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.Location;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.WeatherData;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.repository.LocationRepository;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.repository.WeatherDataRepository;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service.WeatherService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WeatherServiceImpl implements WeatherService {

    private final WeatherDataRepository weatherDataRepository;
    private final LocationRepository locationRepository;
    private final WeatherClient weatherClient;
    private final WeatherDataMapper weatherDataMapper;

    @Override
    @Transactional
    public WeatherDataResponse fetchAndStore(Long locationId) {
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new ResourceNotFoundException("Location not found with id: " + locationId));

        ExternalWeatherResponse externalData = weatherClient.fetchWeather(location.getLatitude(), location.getLongitude());

        WeatherData weatherData = new WeatherData();
        weatherData.setTemperature(externalData.temperature());
        weatherData.setWindSpeed(externalData.windSpeed());
        weatherData.setHumidity(externalData.humidity());
        weatherData.setPrecipitation(externalData.precipitation());
        weatherData.setCondition(externalData.condition());
        weatherData.setRecordedAt(externalData.recordedAt() != null ? externalData.recordedAt() : LocalDateTime.now());
        weatherData.setLocation(location);

        weatherData = weatherDataRepository.save(weatherData);

        return weatherDataMapper.toResponse(weatherData);
    }

    @Override
    @Transactional(readOnly = true)
    public WeatherDataResponse getLatest(Long locationId) {
        if (!locationRepository.existsById(locationId)) {
            throw new ResourceNotFoundException("Location not found with id: " + locationId);
        }
        WeatherData weatherData = weatherDataRepository.findLatestByLocationId(locationId)
                .orElseThrow(() -> new ResourceNotFoundException("No weather data found for location id: " + locationId));
        return weatherDataMapper.toResponse(weatherData);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WeatherDataResponse> getHistory(Long locationId, LocalDateTime from, LocalDateTime to) {
        if (!locationRepository.existsById(locationId)) {
            throw new ResourceNotFoundException("Location not found with id: " + locationId);
        }
        List<WeatherData> weatherDataList = weatherDataRepository.findByLocationIdAndRecordedAtBetween(locationId, from, to);
        return weatherDataList.stream()
                .map(weatherDataMapper::toResponse)
                .collect(Collectors.toList());
    }
}
