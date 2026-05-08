package mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import mk.ukim.finki.ictpm.evn_grid_faults_prediction_system_backend.model.enums.BatchStatus;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "import_batch")
public class ImportBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "file_type", length = 10)
    private String fileType;

    @Column(name = "total_records")
    private Integer totalRecords;

    @Column(name = "successful_records")
    private Integer successfulRecords;

    @Column(name = "failed_records")
    private Integer failedRecords;

    @Enumerated(EnumType.STRING)
    @Column(name = "import_status", length = 20)
    private BatchStatus importStatus;

    @Column(name = "created_by_user_id")
    private Long createdByUserId;

    @Column(name = "errors", columnDefinition = "TEXT")
    private String errors;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
