package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.client.weather;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.extern.slf4j.Slf4j;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.exception.ExternalApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.time.LocalDateTime;

@Slf4j
@Component
@ConditionalOnProperty(name = "weather.provider", havingValue = "open-meteo")
public class OpenMeteoWeatherProvider implements WeatherProvider {

    private static final String SOURCE_API = "open-meteo";
    private static final String CURRENT_PARAMS =
            "temperature_2m,wind_speed_10m,relative_humidity_2m,precipitation,weather_code";

    private final String baseUrl;
    private final RestClient restClient;

    public OpenMeteoWeatherProvider(@Value("${weather.api.base-url}") String baseUrl) {
        this.baseUrl = baseUrl;
        this.restClient = RestClient.create();
    }

    @Override
    public WeatherFetchResult fetchCurrent(double lat, double lng) {
        try {
            URI uri = URI.create(baseUrl + "/forecast?latitude=" + lat + "&longitude=" + lng
                    + "&current=" + CURRENT_PARAMS);

            OpenMeteoResponse response = restClient.get()
                    .uri(uri)
                    .retrieve()
                    .body(OpenMeteoResponse.class);

            if (response == null || response.current() == null) {
                throw new ExternalApiException(
                        "Open-Meteo returned empty response for lat=" + lat + " lng=" + lng);
            }

            CurrentData current = response.current();
            return new WeatherFetchResult(
                    current.temperature2m(),
                    current.windSpeed10m(),
                    current.relativeHumidity2m() != null ? current.relativeHumidity2m().doubleValue() : null,
                    current.precipitation(),
                    WmoCodeMapper.map(current.weatherCode() != null ? current.weatherCode() : -1),
                    SOURCE_API,
                    LocalDateTime.now()
            );
        } catch (ExternalApiException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Failed to fetch weather from Open-Meteo for lat={} lng={}: {}", lat, lng, e.getMessage());
            throw new ExternalApiException("Weather API unavailable: " + e.getMessage());
        }
    }

    private record OpenMeteoResponse(CurrentData current) {}

    private record CurrentData(
            @JsonProperty("temperature_2m") Double temperature2m,
            @JsonProperty("wind_speed_10m") Double windSpeed10m,
            @JsonProperty("relative_humidity_2m") Integer relativeHumidity2m,
            @JsonProperty("precipitation") Double precipitation,
            @JsonProperty("weather_code") Integer weatherCode
    ) {}
}
