package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.request.UpdateCustomerRequest;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.CustomerResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service.CustomerService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping
//    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<CustomerResponse>> getAll() {
        return ResponseEntity.ok(customerService.getAll());
    }

    @GetMapping("/{id}")
//    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CustomerResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(customerService.getById(id));
    }

    @GetMapping("/me")
//    @PreAuthorize("hasRole('CUSTOMER')")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CustomerResponse> getCurrent() {
        return ResponseEntity.ok(customerService.getCurrent());
    }

    @PutMapping("/{id}")
//    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CustomerResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateCustomerRequest request
    ) {
        return ResponseEntity.ok(customerService.update(id, request));
    }

    @DeleteMapping("/{id}")
//    @PreAuthorize("hasRole('ADMIN')")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        customerService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
