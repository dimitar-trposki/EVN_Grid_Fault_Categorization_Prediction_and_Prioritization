package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service.impl;

import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.StatusHistoryDto;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.Customer;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.FaultReport;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.FaultStatusHistory;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.User;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.enums.FaultStatus;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.repository.FaultStatusHistoryRepository;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service.FaultWorkflowService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class FaultWorkflowServiceImpl implements FaultWorkflowService {

    private final FaultStatusHistoryRepository historyRepo;

    public FaultWorkflowServiceImpl(FaultStatusHistoryRepository historyRepo) {
        this.historyRepo = historyRepo;
    }

    @Override
    public void changeStatus(FaultReport fault, FaultStatus status) {
        changeStatus(fault, status, null, null, null, true);
    }

    @Override
    public void changeStatus(FaultReport fault, FaultStatus status,
                             User changedBy, Customer changedByCustomer,
                             String note, boolean customerVisible) {
        FaultStatusHistory history = new FaultStatusHistory();
        history.setFaultReport(fault);
        history.setFaultStatus(status);
        history.setChangedAt(LocalDateTime.now());
        history.setChangedBy(changedBy);
        history.setChangedByCustomer(changedByCustomer);
        history.setNote(note);
        history.setCustomerVisible(customerVisible);
        historyRepo.save(history);
    }

    @Override
    public List<StatusHistoryDto> getHistory(Long faultId) {
        return historyRepo.findByFaultReportId(faultId)
                .stream()
                .map(h -> new StatusHistoryDto(h.getFaultStatus(), h.getChangedAt()))
                .toList();
    }
}
