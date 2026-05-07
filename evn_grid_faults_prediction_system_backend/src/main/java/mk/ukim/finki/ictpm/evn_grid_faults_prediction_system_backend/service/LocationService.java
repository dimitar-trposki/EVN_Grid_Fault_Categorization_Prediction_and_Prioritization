package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service;

import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.request.CreateLocationRequest;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.request.UpdateLocationRequest;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.LocationResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.Location;

import java.util.List;

public interface LocationService {

    LocationResponse create(CreateLocationRequest request);

    LocationResponse getById(Long id);

    List<LocationResponse> getAll();

    List<LocationResponse> getByRegion(Long regionId);

    LocationResponse update(Long id, UpdateLocationRequest request);

    List<LocationResponse> findAllByRegionId(Long regionId);

    LocationResponse findByAddress(String address);

    List<LocationResponse> findByLongitude(Double longitude);

    List<LocationResponse> findByLatitude(Double latitude);

    LocationResponse findByLongitudeAndLatitude(Double longitude, Double latitude);

    void delete(Long id);
}
