package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.constants.JwtConstants;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.request.LoginRequest;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.request.RegisterCustomerRequest;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.LoginResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@Valid @RequestBody RegisterCustomerRequest dto) {
        return ResponseEntity.ok(userService.register(dto));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest dto) {
        return ResponseEntity.ok(userService.login(dto.email(), dto.password()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        String headerValue = request.getHeader(JwtConstants.HEADER);
        if (headerValue != null && headerValue.startsWith(JwtConstants.TOKEN_PREFIX)) {
            String token = headerValue.substring(JwtConstants.TOKEN_PREFIX.length());
            userService.logout(token);
        }
        return ResponseEntity.noContent().build();
    }
}
