package kz.gov.rfs.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Entity
@Table(name = "contacts")
public class Contact {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String labelRu;

    @Column(nullable = false)
    private String labelKk;

    private String labelEn;

    @Column(nullable = false)
    private String value;

    @Enumerated(EnumType.STRING)
    private ContactType contactType;

    private Integer displayOrder = 0;
}