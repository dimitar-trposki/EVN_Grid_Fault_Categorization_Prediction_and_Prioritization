package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.client.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.time.Instant;

@Slf4j
@Service
public class AiHealthCheckService {

    public record AiHealthStatus(boolean reachable, String message, Instant checkedAt) {}

    private static final Duration CACHE_TTL = Duration.ofSeconds(30);

    private final RestClient restClient;
    private final boolean enabled;

    private volatile AiHealthStatus cachedStatus;
    private volatile Instant cacheExpiry = Instant.EPOCH;

    public AiHealthCheckService(
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

    public AiHealthStatus checkHealth() {
        if (!enabled) {
            return new AiHealthStatus(false, "AI service disabled via configuration", Instant.now());
        }

        Instant now = Instant.now();
        if (cachedStatus != null && now.isBefore(cacheExpiry)) {
            return cachedStatus;
        }

        AiHealthStatus fresh = fetchHealthFromService();
        cachedStatus = fresh;
        cacheExpiry = now.plus(CACHE_TTL);
        return fresh;
    }

    private AiHealthStatus fetchHealthFromService() {
        try {
            restClient.get()
                    .uri("/health")
                    .retrieve()
                    .toBodilessEntity();
            return new AiHealthStatus(true, "AI service is reachable", Instant.now());
        } catch (Exception e) {
            log.warn("AI health check failed: {}", e.getMessage());
            return new AiHealthStatus(false, "AI service unreachable: " + e.getMessage(), Instant.now());
        }
    }
}
