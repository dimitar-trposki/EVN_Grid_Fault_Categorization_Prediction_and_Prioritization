package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.client.weather;

public interface WeatherProvider {

    WeatherFetchResult fetchCurrent(double lat, double lng);
}
