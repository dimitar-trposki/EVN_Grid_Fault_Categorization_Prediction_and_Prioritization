package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.web;

import jakarta.validation.Valid;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.UpdateProfileDto;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.UserProfileDto;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    public ResponseEntity<UserProfileDto> getProfile(Authentication authentication) {
        UserProfileDto profile = userService.getProfile(authentication.getName());
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/profile")
    public ResponseEntity<UserProfileDto> updateProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateProfileDto dto
    ) {
        UserProfileDto updated = userService.updateProfile(authentication.getName(), dto);
        return ResponseEntity.ok(updated);
    }
}
