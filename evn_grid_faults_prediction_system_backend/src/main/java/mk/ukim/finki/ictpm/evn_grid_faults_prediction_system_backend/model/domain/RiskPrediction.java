package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "risk_prediction")
public class RiskPrediction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(
            name = "location_id",
            foreignKey = @ForeignKey(name = "fk_risk_prediction_location")
    )
    private Location location;

    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(
            name = "equipment_id",
            foreignKey = @ForeignKey(name = "fk_risk_prediction_equipment")
    )
    private Equipment equipment;

    private Double riskScore;

    @Column(length = 20)
    private String riskLevel;

    @Column(columnDefinition = "TEXT")
    private String contributingFactors;

    private LocalDateTime predictionDate;

    private Boolean isFallback;
}
