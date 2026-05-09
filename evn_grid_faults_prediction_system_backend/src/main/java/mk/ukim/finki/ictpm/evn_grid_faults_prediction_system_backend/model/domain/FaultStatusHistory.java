package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.enums.FaultStatus;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "fault_status_history")
public class FaultStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "fault_status", nullable = false, length = 20)
    private FaultStatus faultStatus;

    @Column(nullable = false)
    private LocalDateTime changedAt;

    @Column(columnDefinition = "TEXT")
    private String note;

    @ColumnDefault("true")
    @Column(name = "customer_visible", nullable = false)
    private Boolean customerVisible = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "changed_by_user_id",
            foreignKey = @ForeignKey(name = "fk_fault_status_history_changed_by_user")
    )
    private User changedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "changed_by_customer_id",
            foreignKey = @ForeignKey(name = "fk_fault_status_history_changed_by_customer")
    )
    private Customer changedByCustomer;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(
            name = "fault_report_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_fault_status_history_fault_report")
    )
    private FaultReport faultReport;

}
