package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.client.ai.AiClient;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.client.ai.dto.RiskPredictionInput;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.client.ai.dto.RiskPredictionResult;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.RiskPredictionResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.WeatherDataResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.exception.ResourceNotFoundException;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.helpers.RiskPredictionMapper;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.Equipment;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.Location;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.RiskPrediction;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.repository.EquipmentRepository;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.repository.FaultReportRepository;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.repository.LocationRepository;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.repository.RegionRepository;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.repository.RiskPredictionRepository;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service.RiskPredictionService;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service.WeatherService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RiskPredictionServiceImpl implements RiskPredictionService {

    private final LocationRepository locationRepository;
    private final EquipmentRepository equipmentRepository;
    private final RegionRepository regionRepository;
    private final FaultReportRepository faultReportRepository;
    private final RiskPredictionRepository riskPredictionRepository;
    private final WeatherService weatherService;
    private final AiClient aiClient;
    private final RiskPredictionMapper mapper;

    @Override
    @Transactional
    public RiskPredictionResponse predictForLocation(Long locationId) {
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new ResourceNotFoundException("Location", locationId));

        Optional<WeatherDataResponse> weather = weatherService.getLatestForRiskInput(locationId);
        long faultCount = faultReportRepository.countByLocationId(locationId);

        RiskPredictionInput input = buildInput(location, null, faultCount, weather);
        RiskPredictionResult aiResult = aiClient.predictRisk(input);

        RiskPrediction entity = buildEntity(aiResult, location, null);
        return mapper.toResponse(riskPredictionRepository.save(entity));
    }

    @Override
    @Transactional
    public RiskPredictionResponse predictForEquipment(Long equipmentId) {
        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment", equipmentId));

        Location location = equipment.getLocation();
        Optional<WeatherDataResponse> weather = weatherService.getLatestForRiskInput(location.getId());
        long faultCount = faultReportRepository.countByLocationId(location.getId());

        RiskPredictionInput input = buildInput(location, equipment, faultCount, weather);
        RiskPredictionResult aiResult = aiClient.predictRisk(input);

        RiskPrediction entity = buildEntity(aiResult, location, equipment);
        return mapper.toResponse(riskPredictionRepository.save(entity));
    }

    @Override
    @Transactional
    public List<RiskPredictionResponse> predictForRegion(Long regionId) {
        if (!regionRepository.existsById(regionId)) {
            throw new ResourceNotFoundException("Region", regionId);
        }
        List<Location> locations = locationRepository.findAllByRegionId(regionId);
        if (locations.isEmpty()) {
            return List.of();
        }
        // TODO: consider async batching for large regions to avoid holding a long transaction
        return locations.stream()
                .map(loc -> {
                    try {
                        return predictForLocation(loc.getId());
                    } catch (Exception e) {
                        log.warn("Risk prediction failed for location {} in region {}: {}",
                                loc.getId(), regionId, e.getMessage());
                        return null;
                    }
                })
                .filter(r -> r != null)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RiskPredictionResponse getLatestForLocation(Long locationId) {
        if (!locationRepository.existsById(locationId)) {
            throw new ResourceNotFoundException("Location", locationId);
        }
        return riskPredictionRepository.findTopByLocationIdOrderByPredictionDateDesc(locationId)
                .map(mapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No risk prediction found for location " + locationId));
    }

    @Override
    @Transactional(readOnly = true)
    public RiskPredictionResponse getLatestForEquipment(Long equipmentId) {
        if (!equipmentRepository.existsById(equipmentId)) {
            throw new ResourceNotFoundException("Equipment", equipmentId);
        }
        return riskPredictionRepository.findTopByEquipmentIdOrderByPredictionDateDesc(equipmentId)
                .map(mapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No risk prediction found for equipment " + equipmentId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RiskPredictionResponse> getHighRiskZones(int limit) {
        return riskPredictionRepository.findTopRiskZonesWithLocation().stream()
                .limit(limit)
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RiskPredictionResponse> getHistoryForLocation(Long locationId) {
        if (!locationRepository.existsById(locationId)) {
            throw new ResourceNotFoundException("Location", locationId);
        }
        return riskPredictionRepository.findByLocationId(locationId).stream()
                .map(mapper::toResponse)
                .toList();
    }

    private RiskPredictionInput buildInput(
            Location location,
            Equipment equipment,
            long faultCount,
            Optional<WeatherDataResponse> weather) {

        Double weatherTemp = null;
        Double weatherWind = null;
        Double weatherPrecip = null;
        String weatherCond = null;

        if (weather.isPresent()) {
            WeatherDataResponse w = weather.get();
            weatherTemp = w.temperature();
            weatherWind = w.windSpeed();
            weatherPrecip = w.precipitation();
            weatherCond = w.weatherCondition() != null ? w.weatherCondition().name() : null;
        }

        return new RiskPredictionInput(
                location.getId(),
                equipment != null ? equipment.getId() : null,
                location.getLatitude(),
                location.getLongitude(),
                1,              // criticalityLevel — no field on Location; defaulting to 1
                (int) faultCount,
                0.0,            // equipmentAgeYears — no install date on Equipment; defaulting to 0
                weatherTemp,
                weatherWind,
                weatherPrecip,
                weatherCond
        );
    }

    private RiskPrediction buildEntity(RiskPredictionResult result, Location location, Equipment equipment) {
        RiskPrediction entity = new RiskPrediction();
        entity.setLocation(location);
        entity.setEquipment(equipment);
        entity.setRiskScore(result.riskScore());
        entity.setRiskLevel(result.riskLevel());
        entity.setContributingFactors(mapper.serializeFactors(result.contributingFactors()));
        entity.setPredictionDate(LocalDateTime.now());
        entity.setIsFallback(result.isFallback());
        return entity;
    }
}
