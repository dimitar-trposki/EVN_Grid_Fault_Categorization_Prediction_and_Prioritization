package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service;

import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.request.CreateRegionRequest;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.request.UpdateRegionRequest;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.RegionResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.Region;

import java.util.List;

public interface RegionService {

    RegionResponse create(CreateRegionRequest request);

    RegionResponse getById(Long id);

    List<RegionResponse> getAll();

    RegionResponse update(Long id, UpdateRegionRequest request);

    void delete(Long id);

    RegionResponse findByName(String name);

}
