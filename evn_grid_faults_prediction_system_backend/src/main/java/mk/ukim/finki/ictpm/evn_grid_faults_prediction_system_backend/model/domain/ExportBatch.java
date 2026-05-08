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
@Table(name = "export_batch")
public class ExportBatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "file_name", length = 255)
    private String fileName;

    @Column(name = "export_type", length = 30)
    private String exportType;

    @Column(name = "export_format", length = 10)
    private String exportFormat;

    @Enumerated(EnumType.STRING)
    @Column(name = "export_status", length = 20)
    private BatchStatus exportStatus;

    @Column(name = "file_path", length = 500)
    private String filePath;

    @Column(name = "created_by_user_id")
    private Long createdByUserId;

    @Column(nullable = false)
    private LocalDateTime createdAt;
}
