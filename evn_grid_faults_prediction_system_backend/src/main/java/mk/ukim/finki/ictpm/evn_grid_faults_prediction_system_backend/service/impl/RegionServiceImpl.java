package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service.impl;

import lombok.RequiredArgsConstructor;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.request.CreateRegionRequest;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.request.UpdateRegionRequest;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.RegionResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.exception.ConflictException;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.exception.ResourceNotFoundException;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.Region;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.repository.RegionRepository;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service.RegionService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RegionServiceImpl implements RegionService {

    private final RegionRepository regionRepository;

    @Override
    public RegionResponse create(CreateRegionRequest request) {
        if (regionRepository.existsByName(request.name())) {
            throw new ConflictException("Region with name '" + request.name() + "' already exists");
        }
        Region region = new Region();
        region.setName(request.name());
        return map(regionRepository.save(region));
    }

    @Override
    public RegionResponse getById(Long id) {
        return map(regionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Region", id)));
    }

    @Override
    public List<RegionResponse> getAll() {
        return regionRepository.findAll().stream()
                .map(this::map)
                .toList();
    }

    @Override
    public RegionResponse update(Long id, UpdateRegionRequest request) {
        Region region = regionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Region", id));

        if (!region.getName().equals(request.name()) && regionRepository.existsByName(request.name())) {
            throw new ConflictException("Region with name '" + request.name() + "' already exists");
        }

        region.setName(request.name());
        return map(regionRepository.save(region));
    }

    @Override
    public void delete(Long id) {
        if (!regionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Region", id);
        }
        regionRepository.deleteById(id);
    }

    @Override
    public RegionResponse findByName(String name) {
        return map(regionRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Region not found with name: " + name)));
    }

    private RegionResponse map(Region r) {
        return new RegionResponse(r.getId(), r.getName());
    }
}
