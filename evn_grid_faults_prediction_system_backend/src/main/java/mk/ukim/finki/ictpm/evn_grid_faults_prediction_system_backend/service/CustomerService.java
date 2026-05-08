package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service;

import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.request.UpdateCustomerRequest;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.CustomerResponse;

import java.util.List;

public interface CustomerService {

    CustomerResponse getById(Long id);

    CustomerResponse getCurrent();

    List<CustomerResponse> getAll();

    CustomerResponse update(Long id, UpdateCustomerRequest req);

    void delete(Long id);
}
