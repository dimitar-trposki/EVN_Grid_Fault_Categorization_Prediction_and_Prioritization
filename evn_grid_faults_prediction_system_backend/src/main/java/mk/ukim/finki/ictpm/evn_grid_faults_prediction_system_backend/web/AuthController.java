package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.constants.JwtConstants;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.CustomerRegistrationDto;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.LoginRequestDto;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.LoginResponseDto;
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
    public ResponseEntity<LoginResponseDto> register(@Valid @RequestBody CustomerRegistrationDto dto) {
        userService.register(dto);
        LoginResponseDto response = userService.login(dto.getEmail(), dto.getPassword());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto dto) {
        LoginResponseDto response = userService.login(dto.getEmail(), dto.getPassword());
        return ResponseEntity.ok(response);
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
