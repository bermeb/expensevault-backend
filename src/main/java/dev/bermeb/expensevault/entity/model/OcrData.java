package dev.bermeb.expensevault.entity.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "orc_data")
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OcrData extends AuditableEntity {

    @Id
    @GeneratedValue(generator = "UUID")
    @Column(columnDefinition = "uuid", updatable = false, nullable = false)
    private UUID id;

    @OneToOne
    @JoinColumn(name = "receipt_id", nullable = false)
    private Receipt receipt;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String rawText;

    @Column(precision = 3, scale = 2)
    private Float confidence;

    @Column(name = "processed_at", nullable = false)
    private LocalDateTime processedAt;

    @Column(columnDefinition = "jsonb")
    private String extractedFields;

    @Column(name = "processing_time_ms")
    private Integer processingTimeMs;

}
