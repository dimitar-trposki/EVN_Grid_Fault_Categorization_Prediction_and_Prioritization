package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.client.weather;

import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.enums.WeatherCondition;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WmoCodeMapperTest {

    @Test
    void mapsKnownCodesToCorrectConditions() {
        assertEquals(WeatherCondition.SUNNY, WmoCodeMapper.map(0));
        assertEquals(WeatherCondition.SUNNY, WmoCodeMapper.map(1));
        assertEquals(WeatherCondition.CLOUDY, WmoCodeMapper.map(2));
        assertEquals(WeatherCondition.CLOUDY, WmoCodeMapper.map(3));
        assertEquals(WeatherCondition.CLOUDY, WmoCodeMapper.map(45));
        assertEquals(WeatherCondition.CLOUDY, WmoCodeMapper.map(48));
        assertEquals(WeatherCondition.RAINY, WmoCodeMapper.map(51));
        assertEquals(WeatherCondition.RAINY, WmoCodeMapper.map(53));
        assertEquals(WeatherCondition.RAINY, WmoCodeMapper.map(61));
        assertEquals(WeatherCondition.RAINY, WmoCodeMapper.map(65));
        assertEquals(WeatherCondition.RAINY, WmoCodeMapper.map(66));
        assertEquals(WeatherCondition.RAINY, WmoCodeMapper.map(80));
        assertEquals(WeatherCondition.RAINY, WmoCodeMapper.map(82));
        assertEquals(WeatherCondition.SNOW, WmoCodeMapper.map(71));
        assertEquals(WeatherCondition.SNOW, WmoCodeMapper.map(75));
        assertEquals(WeatherCondition.SNOW, WmoCodeMapper.map(77));
        assertEquals(WeatherCondition.SNOW, WmoCodeMapper.map(85));
        assertEquals(WeatherCondition.SNOW, WmoCodeMapper.map(86));
        assertEquals(WeatherCondition.STORM, WmoCodeMapper.map(95));
        assertEquals(WeatherCondition.STORM, WmoCodeMapper.map(96));
        assertEquals(WeatherCondition.STORM, WmoCodeMapper.map(99));
    }

    @Test
    void mapsUnknownCodeToCloudy() {
        assertEquals(WeatherCondition.CLOUDY, WmoCodeMapper.map(100));
        assertEquals(WeatherCondition.CLOUDY, WmoCodeMapper.map(-1));
        assertEquals(WeatherCondition.CLOUDY, WmoCodeMapper.map(50));
    }
}
