package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.mapper;

import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.dto.response.WeatherDataResponse;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain.WeatherData;
import org.springframework.stereotype.Component;

@Component
public class WeatherDataMapper {

    public WeatherDataResponse toResponse(WeatherData weatherData) {
        if (weatherData == null) {
            return null;
        }
        return new WeatherDataResponse(
                weatherData.getTemperature(),
                weatherData.getWindSpeed(),
                weatherData.getHumidity(),
                weatherData.getPrecipitation(),
                weatherData.getCondition(),
                weatherData.getRecordedAt()
        );
    }
}
