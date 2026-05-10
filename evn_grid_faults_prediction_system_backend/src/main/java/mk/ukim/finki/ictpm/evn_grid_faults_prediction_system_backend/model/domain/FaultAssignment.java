package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.enums.FaultStatus;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "fault_assignment")
public class FaultAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(
            name = "fault_report_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_fault_assignment_fault_report")
    )
    private FaultReport faultReport;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(
            name = "crew_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_fault_assignment_crew")
    )
    private Crew crew;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "assigned_by_user_id",
            foreignKey = @ForeignKey(name = "fk_fault_assignment_assigned_by")
    )
    private User assignedByUser;

    @Column(name = "assigned_at", nullable = false)
    private LocalDateTime assignedAt;

    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "assignment_status", nullable = false, length = 20)
    private String assignmentStatus;

    @Column(name = "assignment_note", columnDefinition = "TEXT")
    private String assignmentNote;

    /** @deprecated Kept for backward compat with dashboard queries; use assignmentStatus for Module 11 logic */
    @Deprecated
    @Enumerated(EnumType.STRING)
    @Column(name = "fault_status", length = 20)
    private FaultStatus faultStatus;

}
