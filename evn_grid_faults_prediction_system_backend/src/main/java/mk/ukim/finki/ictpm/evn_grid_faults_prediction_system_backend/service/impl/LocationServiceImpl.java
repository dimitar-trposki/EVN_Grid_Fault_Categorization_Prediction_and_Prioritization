package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service.impl;

import lombok.RequiredArgsConstructor;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.request.CreateLocationRequest;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.request.UpdateLocationRequest;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.LocationResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.exception.ResourceNotFoundException;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.Location;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.Region;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.repository.LocationRepository;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.repository.RegionRepository;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service.LocationService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LocationServiceImpl implements LocationService {

    private final LocationRepository locationRepository;
    private final RegionRepository regionRepository;

    @Override
    public LocationResponse create(CreateLocationRequest request) {
        Region region = regionRepository.findById(request.regionId())
                .orElseThrow(() -> new ResourceNotFoundException("Region", request.regionId()));

        Location location = new Location();
        location.setLatitude(request.latitude());
        location.setLongitude(request.longitude());
        location.setAddress(request.address());
        location.setRegion(region);

        return map(locationRepository.save(location));
    }

    @Override
    public LocationResponse getById(Long id) {
        return map(locationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Location", id)));
    }

    @Override
    public List<LocationResponse> getAll() {
        return locationRepository.findAll().stream()
                .map(this::map)
                .toList();
    }

    @Override
    public List<LocationResponse> getByRegion(Long regionId) {
        return locationRepository.findAllByRegionId(regionId).stream()
                .map(this::map)
                .toList();
    }

    @Override
    public LocationResponse update(Long id, UpdateLocationRequest request) {
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Location", id));

        Region region = regionRepository.findById(request.regionId())
                .orElseThrow(() -> new ResourceNotFoundException("Region", request.regionId()));

        location.setLatitude(request.latitude());
        location.setLongitude(request.longitude());
        location.setAddress(request.address());
        location.setRegion(region);

        return map(locationRepository.save(location));
    }

    @Override
    public List<LocationResponse> findAllByRegionId(Long regionId) {
        return locationRepository.findAllByRegionId(regionId).stream()
                .map(this::map)
                .toList();
    }

    @Override
    public LocationResponse findByAddress(String address) {
        return map(locationRepository.findByAddress(address)
                .orElseThrow(() -> new ResourceNotFoundException("Location not found with address: " + address)));
    }

    @Override
    public List<LocationResponse> findByLongitude(Double longitude) {
        return locationRepository.findByLongitude(longitude).stream()
                .map(this::map)
                .toList();
    }

    @Override
    public List<LocationResponse> findByLatitude(Double latitude) {
        return locationRepository.findByLatitude(latitude).stream()
                .map(this::map)
                .toList();
    }

    @Override
    public LocationResponse findByLongitudeAndLatitude(Double longitude, Double latitude) {
        return map(locationRepository.findByLongitudeAndLatitude(longitude, latitude)
                .orElseThrow(() -> new ResourceNotFoundException("Location not found at given coordinates")));
    }

    @Override
    public void delete(Long id) {
        if (!locationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Location", id);
        }
        locationRepository.deleteById(id);
    }

    private LocationResponse map(Location l) {
        return new LocationResponse(
                l.getId(),
                l.getLatitude(),
                l.getLongitude(),
                l.getAddress(),
                l.getRegion().getId(),
                l.getRegion().getName()
        );
    }
}
