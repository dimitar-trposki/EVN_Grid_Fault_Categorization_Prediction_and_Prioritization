package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.enums.FaultPriority;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "fault_priority_record")
public class FaultPriorityRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(
            name = "fault_report_id",
            nullable = false,
            unique = true,
            foreignKey = @ForeignKey(name = "fk_fault_priority_record_fault_report")
    )
    private FaultReport faultReport;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private FaultPriority priorityLevel;

    private Double priorityScore;

    @Column(columnDefinition = "TEXT")
    private String explanation;

    private LocalDateTime calculatedAt;

    @Column(length = 20)
    private String calculationSource;

    private Boolean isFallback;
}
