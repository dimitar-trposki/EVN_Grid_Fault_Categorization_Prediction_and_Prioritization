package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.util;

import lombok.RequiredArgsConstructor;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.repository.FaultReportRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TrackingCodeGenerator {

    private final FaultReportRepository faultReportRepository;

    public String generate() {
        for (int i = 0; i < 5; i++) {
            String code = buildCode();
            if (!faultReportRepository.existsByTrackingCode(code)) {
                return code;
            }
        }
        return buildCode() + UUID.randomUUID().toString().replace("-", "").substring(0, 4).toUpperCase();
    }

    private String buildCode() {
        String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 5).toUpperCase();
        return "FLT-" + date + "-" + suffix;
    }
}
