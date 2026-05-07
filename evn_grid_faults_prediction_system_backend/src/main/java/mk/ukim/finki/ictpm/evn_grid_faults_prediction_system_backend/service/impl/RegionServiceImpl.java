package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service.impl;

import lombok.RequiredArgsConstructor;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.request.CreateRegionRequest;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.request.UpdateRegionRequest;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.RegionResponse;
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
        Region region = new Region();
        region.setName(request.name());

        return map(regionRepository.save(region));
    }

    @Override
    public RegionResponse getById(Long id) {
        Region region = regionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Region not found"));

        return map(region);
    }

    @Override
    public List<RegionResponse> getAll() {
        return regionRepository.findAll()
                .stream()
                .map(this::map)
                .toList();
    }

    @Override
    public RegionResponse update(Long id, UpdateRegionRequest request) {
        Region region = regionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Region not found"));

        region.setName(request.name());

        return map(regionRepository.save(region));
    }

    @Override
    public void delete(Long id) {
        regionRepository.deleteById(id);
    }

    @Override
    public RegionResponse findByName(String name) {
        Region region = regionRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("Region not found"));

        return map(region);
    }


    private RegionResponse map(Region r) {
        return new RegionResponse(r.getId(), r.getName());
    }
}