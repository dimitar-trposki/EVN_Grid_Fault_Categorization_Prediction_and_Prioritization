package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.enums.FaultClassification;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.enums.FaultPriority;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.enums.FaultType;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "fault_report")
public class FaultReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String title;

    @Column(nullable = false)
    private String description;

    @ManyToOne
    private Customer customer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FaultType faultType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FaultPriority faultPriority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FaultClassification faultClassification;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(
            name = "location_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_fault_report_location")
    )
    private Location location;

    @OneToMany(mappedBy = "faultReport", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FaultAssignment> faultAssignments;

    @OneToMany(mappedBy = "faultReport", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Intervention> interventions;

    @OneToMany(mappedBy = "faultReport", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FaultStatusHistory> faultStatusHistories;

}
