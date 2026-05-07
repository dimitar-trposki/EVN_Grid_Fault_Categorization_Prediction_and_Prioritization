package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service;

import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.CreateFaultReportDto;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.FaultReportResponseDto;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.enums.FaultStatus;

import java.util.List;

public interface FaultReportService {

    FaultReportResponseDto createFault(CreateFaultReportDto dto);

    List<FaultReportResponseDto> getAll();

    FaultReportResponseDto getById(Long id);

    FaultReportResponseDto changeStatus(Long id, FaultStatus status);
}