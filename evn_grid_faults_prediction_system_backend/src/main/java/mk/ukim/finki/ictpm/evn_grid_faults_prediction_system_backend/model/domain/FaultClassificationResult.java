package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "fault_classification_result")
public class FaultClassificationResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(
            name = "fault_report_id",
            nullable = false,
            unique = true,
            foreignKey = @ForeignKey(name = "fk_classification_result_fault_report")
    )
    private FaultReport faultReport;

    @Column(length = 50)
    private String predictedFaultCategory;

    @Column(length = 20)
    private String predictedSeverity;

    private Double classificationConfidence;

    @ElementCollection
    @CollectionTable(
            name = "fault_classification_result_keywords",
            joinColumns = @JoinColumn(name = "classification_result_id")
    )
    @Column(name = "keyword", length = 100)
    private List<String> extractedKeywords = new ArrayList<>();

    private Boolean safetyRisk;
    private Boolean nlpProcessed;
    private LocalDateTime classifiedAt;
    private Boolean isFallback;
}
