package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service;

import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.request.RegisterCustomerRequest;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.request.RegisterUserRequest;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.request.UpdateProfileRequest;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.request.UpdateRoleRequest;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.LoginResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.UserProfileResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.UserSummaryResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;
import java.util.Optional;

public interface UserService extends UserDetailsService {

    LoginResponse register(RegisterCustomerRequest dto);

    LoginResponse login(String email, String password);

    void logout(String token);

    Optional<User> findByEmail(String email);

    UserProfileResponse getProfile(String email);

    UserProfileResponse updateProfile(String email, UpdateProfileRequest dto);

    UserProfileResponse createInternalUser(RegisterUserRequest request);

    List<UserSummaryResponse> getAllUsers();

    UserProfileResponse getUserById(Long id);

    UserProfileResponse updateUserRole(Long id, UpdateRoleRequest request);
}
