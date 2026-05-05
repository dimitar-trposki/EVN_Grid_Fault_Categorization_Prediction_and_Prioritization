package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service;

import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.CrewResponseDto;

import java.util.List;

public interface CrewService {
    List<CrewResponseDto> getAll();
    CrewResponseDto getById(Long id);
    CrewResponseDto assignToFault(Long faultId, Long crewId);
}