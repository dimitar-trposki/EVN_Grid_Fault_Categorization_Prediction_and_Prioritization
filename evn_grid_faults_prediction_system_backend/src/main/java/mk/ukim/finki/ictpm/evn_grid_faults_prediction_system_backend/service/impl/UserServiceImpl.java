package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service.impl;

import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.CustomerRegistrationDto;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.LoginResponseDto;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.UpdateProfileDto;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.UserProfileDto;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.helpers.JwtHelper;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.Customer;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.User;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.enums.RoleType;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.repository.CustomerRepository;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.repository.UserRepository;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service.UserService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtHelper jwtHelper;

    public UserServiceImpl(UserRepository userRepository,
                           CustomerRepository customerRepository,
                           PasswordEncoder passwordEncoder,
                           JwtHelper jwtHelper) {
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtHelper = jwtHelper;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

    @Override
    @Transactional
    public User register(CustomerRegistrationDto dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + dto.getEmail());
        }

        User user = new User();
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setUserRole(RoleType.CUSTOMER);

        user = userRepository.save(user);

        Customer customer = new Customer();
        customer.setContact(dto.getPhone());
        customer.setUser(user);

        customerRepository.save(customer);

        return user;
    }

    @Override
    public LoginResponseDto login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("Invalid password");
        }

        String token = jwtHelper.generateToken(user);

        return new LoginResponseDto(
                token,
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getUserRole()
        );
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public UserProfileDto getProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        String phone = null;
        if (user.getCustomer() != null) {
            phone = user.getCustomer().getContact();
        }

        return new UserProfileDto(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getUserRole(),
                phone
        );
    }

    @Override
    @Transactional
    public UserProfileDto updateProfile(String email, UpdateProfileDto dto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        userRepository.save(user);

        if (user.getCustomer() != null && dto.getPhone() != null) {
            Customer customer = user.getCustomer();
            customer.setContact(dto.getPhone());
            customerRepository.save(customer);
        }

        return getProfile(email);
    }
}
