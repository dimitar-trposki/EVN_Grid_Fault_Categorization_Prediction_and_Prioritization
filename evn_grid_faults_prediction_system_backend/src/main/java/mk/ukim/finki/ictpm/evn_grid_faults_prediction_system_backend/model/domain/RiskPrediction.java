package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "risk_prediction")
public class RiskPrediction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double probability;

    private String recommendation;

    @OneToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(
            name = "fault_report_id",
            nullable = false,
            unique = true,
            foreignKey = @ForeignKey(name = "fk_risk_prediction_fault_report")
    )
    private FaultReport faultReport;

}
