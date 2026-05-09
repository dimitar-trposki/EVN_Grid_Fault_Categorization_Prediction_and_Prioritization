package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.client.ai;

import lombok.extern.slf4j.Slf4j;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.client.ai.dto.ClassificationInput;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.client.ai.dto.ClassificationResult;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.client.ai.dto.PriorityCalculationInput;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.client.ai.dto.PriorityCalculationResult;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.client.ai.dto.RiskPredictionInput;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.client.ai.dto.RiskPredictionResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Slf4j
@Component
public class AiClient {

    private final RestClient restClient;
    private final boolean enabled;

    public AiClient(
            @Value("${ai.service.base-url}") String baseUrl,
            @Value("${ai.service.timeout-seconds}") int timeoutSeconds,
            @Value("${ai.service.enabled:true}") boolean enabled) {
        this.enabled = enabled;
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(timeoutSeconds));
        factory.setReadTimeout(Duration.ofSeconds(timeoutSeconds));
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .build();
    }

    public ClassificationResult classifyFault(ClassificationInput input) {
        if (!enabled) return ClassificationResult.fallback();
        try {
            ClassificationResult result = restClient.post()
                    .uri("/classify")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(input)
                    .retrieve()
                    .body(ClassificationResult.class);
            return result != null ? result : ClassificationResult.fallback();
        } catch (Exception e) {
            log.warn("AI classify failed for faultId={}: {}", input.faultId(), e.getMessage());
            return ClassificationResult.fallback();
        }
    }

    public RiskPredictionResult predictRisk(RiskPredictionInput input) {
        if (!enabled) return RiskPredictionResult.fallback();
        try {
            RiskPredictionResult result = restClient.post()
                    .uri("/predict-risk")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(input)
                    .retrieve()
                    .body(RiskPredictionResult.class);
            return result != null ? result : RiskPredictionResult.fallback();
        } catch (Exception e) {
            log.warn("AI predict-risk failed for locationId={}: {}", input.locationId(), e.getMessage());
            return RiskPredictionResult.fallback();
        }
    }

    public PriorityCalculationResult calculatePriority(PriorityCalculationInput input) {
        if (!enabled) return PriorityCalculationResult.fallback();
        try {
            PriorityCalculationResult result = restClient.post()
                    .uri("/calculate-priority")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(input)
                    .retrieve()
                    .body(PriorityCalculationResult.class);
            return result != null ? result : PriorityCalculationResult.fallback();
        } catch (Exception e) {
            log.warn("AI calculate-priority failed for faultId={}: {}", input.faultId(), e.getMessage());
            return PriorityCalculationResult.fallback();
        }
    }
}
