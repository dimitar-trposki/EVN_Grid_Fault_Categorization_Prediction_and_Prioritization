package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service.impl;

import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.request.RegisterCustomerRequest;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.request.RegisterUserRequest;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.request.UpdateProfileRequest;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.request.UpdateRoleRequest;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.LoginResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.UserProfileResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.UserSummaryResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.exception.BadRequestException;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.exception.ConflictException;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.exception.ResourceNotFoundException;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.helpers.JwtHelper;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.Customer;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.User;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.enums.RoleType;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.repository.CustomerRepository;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.repository.UserRepository;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service.TokenBlacklistService;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service.UserService;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.constants.JwtConstants;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtHelper jwtHelper;
    private final TokenBlacklistService tokenBlacklistService;

    public UserServiceImpl(UserRepository userRepository,
                           CustomerRepository customerRepository,
                           PasswordEncoder passwordEncoder,
                           JwtHelper jwtHelper,
                           TokenBlacklistService tokenBlacklistService) {
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtHelper = jwtHelper;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

    @Override
    @Transactional
    public LoginResponse register(RegisterCustomerRequest dto) {
        if (userRepository.existsByEmail(dto.email())) {
            throw new ConflictException("Email already registered: " + dto.email());
        }

        User user = new User();
        user.setFirstName(dto.firstName());
        user.setLastName(dto.lastName());
        user.setEmail(dto.email());
        user.setPassword(passwordEncoder.encode(dto.password()));
        user.setUserRole(RoleType.CUSTOMER);
        user = userRepository.save(user);

        Customer customer = new Customer();
        customer.setContact(dto.phone());
        customer.setUser(user);
        customerRepository.save(customer);

        String token = jwtHelper.generateToken(user);
        return new LoginResponse(token, user.getId(), user.getEmail(), user.getFirstName(), user.getLastName(), user.getUserRole());
    }

    @Override
    public LoginResponse login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BadRequestException("Invalid credentials");
        }

        String token = jwtHelper.generateToken(user);
        return new LoginResponse(token, user.getId(), user.getEmail(), user.getFirstName(), user.getLastName(), user.getUserRole());
    }

    @Override
    public void logout(String token) {
        try {
            Date expiration = jwtHelper.extractExpiration(token);
            tokenBlacklistService.blacklist(token, expiration);
        } catch (Exception e) {
            tokenBlacklistService.blacklist(token, new Date(System.currentTimeMillis() + JwtConstants.EXPIRATION_TIME));
        }
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public UserProfileResponse getProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return toProfileResponse(user);
    }

    @Override
    @Transactional
    public UserProfileResponse updateProfile(String email, UpdateProfileRequest dto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        user.setFirstName(dto.firstName());
        user.setLastName(dto.lastName());
        userRepository.save(user);

        if (user.getCustomer() != null && dto.phone() != null) {
            Customer customer = user.getCustomer();
            customer.setContact(dto.phone());
            customerRepository.save(customer);
        }

        return toProfileResponse(user);
    }

    @Override
    @Transactional
    public UserProfileResponse createInternalUser(RegisterUserRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("Email already registered: " + request.email());
        }

        User user = new User();
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setUserRole(request.role());
        user = userRepository.save(user);

        return toProfileResponse(user);
    }

    @Override
    public List<UserSummaryResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(u -> new UserSummaryResponse(u.getId(), u.getFirstName(), u.getLastName(), u.getEmail(), u.getUserRole()))
                .toList();
    }

    @Override
    public UserProfileResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        return toProfileResponse(user);
    }

    @Override
    @Transactional
    public UserProfileResponse updateUserRole(Long id, UpdateRoleRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        user.setUserRole(request.role());
        userRepository.save(user);
        return toProfileResponse(user);
    }

    private UserProfileResponse toProfileResponse(User user) {
        String contact = user.getCustomer() != null ? user.getCustomer().getContact() : null;
        return new UserProfileResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getUserRole(),
                contact
        );
    }
}
