package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.web;

import lombok.RequiredArgsConstructor;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.FaultPriorityResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service.FaultPriorityService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/priorities")
@RequiredArgsConstructor
public class PriorityQueryController {

    private final FaultPriorityService faultPriorityService;

    @GetMapping("/top")
//    @PreAuthorize("hasAnyRole('DISPATCHER', 'MANAGER', 'ADMIN')")
    @PreAuthorize("isAuthenticated()")
        public ResponseEntity<List<FaultPriorityResponse>> getTop(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(faultPriorityService.getTopPriorityFaults(limit));
    }
}
