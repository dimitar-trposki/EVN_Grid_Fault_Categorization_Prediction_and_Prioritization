package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service;

import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.RiskPredictionResponse;

import java.util.List;

public interface RiskPredictionService {

    RiskPredictionResponse predictForLocation(Long locationId);

    RiskPredictionResponse predictForEquipment(Long equipmentId);

    List<RiskPredictionResponse> predictForRegion(Long regionId);

    RiskPredictionResponse getLatestForLocation(Long locationId);

    RiskPredictionResponse getLatestForEquipment(Long equipmentId);

    List<RiskPredictionResponse> getHighRiskZones(int limit);

    List<RiskPredictionResponse> getHistoryForLocation(Long locationId);
}
