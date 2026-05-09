package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "location")
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private Double latitude;

    @Column(nullable = false, length = 80)
    private Double longitude;

    @Column(nullable = false, length = 100)
    private String address;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(
            name = "region_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_location_region")
    )
    private Region region;

    @OneToMany(mappedBy = "location", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Equipment> equipments;

//    @OneToMany(mappedBy = "location", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<WeatherData> weatherData;

}
