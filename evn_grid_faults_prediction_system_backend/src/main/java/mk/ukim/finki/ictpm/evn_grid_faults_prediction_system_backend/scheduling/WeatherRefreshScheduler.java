package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.scheduling;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.service.WeatherService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "weather.refresh.enabled", havingValue = "true")
public class WeatherRefreshScheduler {

    private final WeatherService weatherService;

    @Scheduled(cron = "${weather.refresh.cron}")
    public void refreshWeatherForAllLocations() {
        log.info("Scheduled weather refresh started");
        int count = weatherService.fetchAndStoreForAll();
        log.info("Scheduled weather refresh complete: {} locations updated", count);
    }
}
