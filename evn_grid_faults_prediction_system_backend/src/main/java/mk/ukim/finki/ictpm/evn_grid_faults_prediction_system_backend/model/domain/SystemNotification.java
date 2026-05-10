package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.enums.NotificationStatus;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "system_notification")
public class SystemNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 200)
    private String title;

    @Column(nullable = false)
    private String message;

    @Column(length = 100)
    private String type;

    @Column(length = 50)
    private String channel;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_status", nullable = false, length = 20)
    private NotificationStatus notificationStatus;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id",
            foreignKey = @ForeignKey(name = "fk_system_notification_user")
    )
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "customer_id",
            foreignKey = @ForeignKey(name = "fk_system_notification_customer")
    )
    private Customer customer;
}
