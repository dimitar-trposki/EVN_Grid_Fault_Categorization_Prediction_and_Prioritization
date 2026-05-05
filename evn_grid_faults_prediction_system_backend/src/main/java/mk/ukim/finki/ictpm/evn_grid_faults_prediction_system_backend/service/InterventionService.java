package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service;

import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.InterventionCreateDto;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.InterventionResponseDto;

import java.util.List;

public interface InterventionService {
    InterventionResponseDto create(Long faultId, InterventionCreateDto dto);
    List<InterventionResponseDto> getByFault(Long faultId);
}