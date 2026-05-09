package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.enums.WeatherCondition;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "weather_data",
        indexes = {
                @Index(name = "idx_weather_data_location_recorded", columnList = "location_id, recorded_at")
        }
)
public class WeatherData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(
            name = "location_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_weather_data_location")
    )
    private Location location;

    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;

    /** Temperature in °C. */
    @Column(name = "temperature")
    private Double temperature;

    /** Wind speed in m/s. */
    @Column(name = "wind_speed")
    private Double windSpeed;

    /** Relative humidity as a percentage (0–100). */
    @Column(name = "humidity")
    private Double humidity;

    /** Precipitation in mm. */
    @Column(name = "precipitation")
    private Double precipitation;

    @Enumerated(EnumType.STRING)
    @Column(name = "weather_condition", length = 20)
    private WeatherCondition weatherCondition;

    @Column(name = "source_api", length = 50)
    private String sourceApi;
}
