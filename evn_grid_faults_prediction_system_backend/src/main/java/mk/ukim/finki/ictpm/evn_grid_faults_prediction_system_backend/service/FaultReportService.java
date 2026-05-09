package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service;

import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.CreateFaultReportDto;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.FaultReportResponseDto;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.request.CreateFaultReportRequest;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.request.FaultStatusUpdateRequest;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.request.OperatorCreateFaultRequest;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.request.UpdateFaultReportRequest;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.FaultReportResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.FaultReportSummaryResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.FaultStatusHistoryResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.TrackFaultResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.enums.FaultClassification;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.enums.FaultPriority;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.enums.FaultStatus;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.enums.FaultType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface FaultReportService {

    /** @deprecated Use {@link #createByCustomer} or {@link #createByOperator} */
    @Deprecated
    FaultReportResponseDto createFault(CreateFaultReportDto dto);

    /** @deprecated Use {@link #getFiltered} */
    @Deprecated
    List<FaultReportResponseDto> getAll();

    /** @deprecated Use {@link #getFaultById} */
    @Deprecated
    FaultReportResponseDto getById(Long id);

    /** @deprecated Use {@link #updateStatus} */
    @Deprecated
    FaultReportResponseDto changeStatus(Long id, FaultStatus status);

    FaultReportResponse createByCustomer(CreateFaultReportRequest dto, String callerEmail);

    FaultReportResponse createByOperator(OperatorCreateFaultRequest dto, String callerEmail);

    Page<FaultReportSummaryResponse> getFiltered(
            FaultType faultType,
            FaultPriority faultPriority,
            FaultClassification faultClassification,
            FaultStatus status,
            Long locationId,
            Long regionId,
            Long customerId,
            LocalDateTime from,
            LocalDateTime to,
            Pageable pageable);

    List<FaultReportSummaryResponse> getMyFaults(String callerEmail);

    FaultReportResponse getFaultById(Long id);

    TrackFaultResponse getByTrackingCode(String trackingCode);

    FaultReportResponse updateFault(Long id, UpdateFaultReportRequest dto);

    void updateStatus(Long id, FaultStatusUpdateRequest dto, String callerEmail);

    List<FaultStatusHistoryResponse> getStatusHistory(Long id);

    void delete(Long id);
}
