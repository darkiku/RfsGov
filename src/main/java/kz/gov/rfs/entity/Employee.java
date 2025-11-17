package kz.gov.rfs.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Entity
@Table(name = "employees")
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullNameRu;

    @Column(nullable = false)
    private String fullNameKk;

    private String fullNameEn;

    private String positionRu;

    private String positionKk;

    private String positionEn;

    private String phone;

    private String email;

    private String photoUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    @JsonIgnore
    private Department department;

    private Integer displayOrder = 0;
}