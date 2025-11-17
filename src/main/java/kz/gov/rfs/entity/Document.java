package kz.gov.rfs.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
@Data
@Entity
@Table(name = "documents")
public class Document {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titleRu;

    @Column(nullable = false)
    private String titleKk;

    private String titleEn;

    @Column(columnDefinition = "TEXT")
    private String descriptionRu;

    @Column(columnDefinition = "TEXT")
    private String descriptionKk;

    @Column(columnDefinition = "TEXT")
    private String descriptionEn;

    @Column(nullable = false)
    private String fileUrl;

    private String fileName;

    private Long fileSize;

    @Enumerated(EnumType.STRING)
    private DocumentType documentType;

    private LocalDateTime uploadDate;

    private Boolean isActive = true;

    @PrePersist
    protected void onCreate() {
        uploadDate = LocalDateTime.now();
    }
}