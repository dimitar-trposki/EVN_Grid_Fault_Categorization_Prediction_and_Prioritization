package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service;

import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.StatusHistoryDto;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.Customer;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.FaultReport;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.User;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.enums.FaultStatus;

import java.util.List;

public interface FaultWorkflowService {

    void changeStatus(FaultReport fault, FaultStatus status);

    void changeStatus(FaultReport fault, FaultStatus status,
                      User changedBy, Customer changedByCustomer,
                      String note, boolean customerVisible);

    List<StatusHistoryDto> getHistory(Long faultId);
}
