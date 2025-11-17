package kz.gov.rfs.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "procurements")
public class Procurement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titleRu;

    private String titleKk;

    private String titleEn;

    @Column(columnDefinition = "TEXT")
    private String descriptionRu;

    @Column(columnDefinition = "TEXT")
    private String descriptionKk;

    @Column(columnDefinition = "TEXT")
    private String descriptionEn;

    private Integer year;

    private LocalDate publishDate;

    private LocalDate deadline;

    private String documentUrl;

    @Enumerated(EnumType.STRING)
    private ProcurementType procurementType;

    private Boolean isActive = true;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}