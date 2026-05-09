package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.helpers;

import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.FaultStatusHistoryResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.FaultStatusHistory;
import org.springframework.stereotype.Component;

@Component
public class FaultStatusHistoryMapper {

    public FaultStatusHistoryResponse toResponse(FaultStatusHistory h) {
        String changedByName = null;
        if (h.getChangedBy() != null) {
            changedByName = h.getChangedBy().getFirstName() + " " + h.getChangedBy().getLastName();
        } else if (h.getChangedByCustomer() != null && h.getChangedByCustomer().getUser() != null) {
            changedByName = h.getChangedByCustomer().getUser().getFirstName()
                    + " " + h.getChangedByCustomer().getUser().getLastName();
        }
        return new FaultStatusHistoryResponse(
                h.getId(),
                h.getFaultStatus(),
                h.getChangedAt(),
                h.getNote(),
                h.getCustomerVisible(),
                changedByName
        );
    }
}
