package kz.gov.rfs.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Entity
@Table(name = "about")
public class About {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String sectionKey;

    @Column(nullable = false)
    private String titleRu;

    @Column(nullable = false)
    private String titleKk;

    private String titleEn;

    @Column(columnDefinition = "TEXT")
    private String contentRu;

    @Column(columnDefinition = "TEXT")
    private String contentKk;

    @Column(columnDefinition = "TEXT")
    private String contentEn;

    @Enumerated(EnumType.STRING)
    private AboutSection section;

    private Integer displayOrder = 0;
}