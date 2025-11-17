package kz.gov.rfs.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Entity
@Table(name = "services")
public class Service {
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

    private String iconUrl;

    private String link;

    private Integer displayOrder = 0;

    private Boolean isActive = true;

    @Enumerated(EnumType.STRING)
    private ServiceType serviceType;
}