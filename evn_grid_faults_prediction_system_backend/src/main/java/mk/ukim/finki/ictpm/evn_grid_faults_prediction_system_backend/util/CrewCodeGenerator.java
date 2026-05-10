package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.util;

import lombok.RequiredArgsConstructor;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.repository.CrewRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CrewCodeGenerator {

    private final CrewRepository crewRepository;

    public String generate() {
        for (int i = 0; i < 5; i++) {
            String code = buildCode();
            if (!crewRepository.existsByCrewCode(code)) {
                return code;
            }
        }
        return buildCode() + UUID.randomUUID().toString().replace("-", "").substring(0, 4).toUpperCase();
    }

    private String buildCode() {
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 4).toUpperCase();
        return "CREW-" + suffix;
    }
}
