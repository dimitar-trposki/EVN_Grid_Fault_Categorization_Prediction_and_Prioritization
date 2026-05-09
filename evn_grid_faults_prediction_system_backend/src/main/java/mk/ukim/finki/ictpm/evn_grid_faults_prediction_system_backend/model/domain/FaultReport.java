package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.enums.FaultClassification;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.enums.FaultPriority;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.enums.FaultSourceType;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.enums.FaultType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "fault_report",
        indexes = {
                @Index(name = "idx_fault_report_tracking_code", columnList = "tracking_code", unique = true),
                @Index(name = "idx_fault_report_reported_at", columnList = "reported_at")
        }
)
public class FaultReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tracking_code", nullable = false, length = 30, unique = true)
    private String trackingCode;

    @Column(name = "reported_at", nullable = false)
    private LocalDateTime reportedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 30)
    private FaultSourceType sourceType;

    @Column(nullable = false, length = 80)
    private String title;

    @Column(nullable = false)
    private String description;

    @ManyToOne
    private Customer customer;

    @PrePersist
    protected void onPrePersist() {
        if (reportedAt == null) reportedAt = LocalDateTime.now();
        if (sourceType == null) sourceType = FaultSourceType.CUSTOMER_PORTAL;
        if (trackingCode == null) {
            String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
            String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 5).toUpperCase();
            trackingCode = "FLT-" + date + "-" + suffix;
        }
    }

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
