package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.client.weather;

import lombok.extern.slf4j.Slf4j;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.enums.WeatherCondition;

/**
 * Maps WMO weather interpretation codes (0–99) to the project's WeatherCondition enum.
 * The project enum has 5 values (SUNNY, CLOUDY, RAINY, SNOW, STORM), so some WMO codes
 * are approximated to the closest semantic match.
 */
@Slf4j
public class WmoCodeMapper {

    private WmoCodeMapper() {}

    public static WeatherCondition map(int code) {
        return switch (code) {
            case 0, 1 -> WeatherCondition.SUNNY;
            case 2, 3, 45, 48 -> WeatherCondition.CLOUDY;
            case 51, 53, 55, 61, 63, 65, 66, 67, 80, 81, 82 -> WeatherCondition.RAINY;
            case 71, 73, 75, 77, 85, 86 -> WeatherCondition.SNOW;
            case 95, 96, 99 -> WeatherCondition.STORM;
            default -> {
                log.warn("Unknown WMO weather code: {}", code);
                yield WeatherCondition.CLOUDY;
            }
        };
    }
}
