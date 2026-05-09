package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.client;

import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.exception.ExternalApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;

@Component
public class WeatherClient {

    private final WebClient webClient;
    private final String apiKey;

    public WeatherClient(
            WebClient.Builder webClientBuilder,
            @Value("${weather.api.base-url:https://api.openweathermap.org/data/2.5}") String baseUrl,
            @Value("${weather.api.key:default_key}") String apiKey) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
        this.apiKey = apiKey;
    }

    public ExternalWeatherResponse fetchWeather(Double latitude, Double longitude) {
        try {
            return webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/weather")
                            .queryParam("lat", latitude)
                            .queryParam("lon", longitude)
                            .queryParam("appid", apiKey)
                            .build())
                    .retrieve()
                    .bodyToMono(ExternalWeatherResponse.class)
                    .block();
        } catch (Exception e) {
            throw new ExternalApiException("Failed to fetch weather data: " + e.getMessage());
        }
    }

    public record ExternalWeatherResponse(
            Double temperature,
            Double windSpeed,
            Double humidity,
            Double precipitation,
            String condition,
            LocalDateTime recordedAt
    ) {
    }
}
