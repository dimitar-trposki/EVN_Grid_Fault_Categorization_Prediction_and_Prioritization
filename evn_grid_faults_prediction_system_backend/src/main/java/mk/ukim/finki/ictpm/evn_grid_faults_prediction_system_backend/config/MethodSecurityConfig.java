package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@Profile({"dev", "prod"})
@Configuration
@EnableMethodSecurity
public class MethodSecurityConfig {
}
