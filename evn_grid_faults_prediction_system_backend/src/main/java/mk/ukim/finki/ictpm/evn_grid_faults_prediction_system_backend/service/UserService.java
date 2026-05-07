package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service;

import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.CustomerRegistrationDto;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.LoginResponseDto;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.UpdateProfileDto;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.UserProfileDto;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Optional;

public interface UserService extends UserDetailsService {

    User register(CustomerRegistrationDto dto);

    LoginResponseDto login(String email, String password);

    Optional<User> findByEmail(String email);

    UserProfileDto getProfile(String email);

    UserProfileDto updateProfile(String email, UpdateProfileDto dto);

    void logout(String token);
}
