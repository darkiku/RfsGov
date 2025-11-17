package kz.gov.rfs.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "news", indexes = {
        @Index(name = "idx_news_published_date", columnList = "publishedDate"),
        @Index(name = "idx_news_is_active", columnList = "isActive"),
        @Index(name = "idx_news_active_date", columnList = "isActive,publishedDate"),
        @Index(name = "idx_news_type", columnList = "newsType")
})
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "newsCache")
public class News {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Title in Russian is required")
    @Size(min = 5, max = 255, message = "Title must be between 5 and 255 characters")
    @Column(nullable = false, length = 255)
    private String titleRu;

    @NotBlank(message = "Title in Kazakh is required")
    @Size(min = 5, max = 255)
    @Column(nullable = false, length = 255)
    private String titleKk;

    @Size(max = 255)
    @Column(length = 255)
    private String titleEn;

    @NotBlank(message = "Content in Russian is required")
    @Column(columnDefinition = "TEXT", nullable = false)
    private String contentRu;

    @NotBlank(message = "Content in Kazakh is required")
    @Column(columnDefinition = "TEXT", nullable = false)
    private String contentKk;

    @Column(columnDefinition = "TEXT")
    private String contentEn;

    @Size(max = 500)
    @Column(length = 500)
    private String imageUrl;

    @Column(nullable = false, updatable = false)
    private LocalDateTime publishedDate;

    @Size(max = 100)
    @Column(length = 100)
    private String author;

    @Column(nullable = false)
    private Boolean isActive = true;

    @Size(max = 500, message = "Short description must not exceed 500 characters")
    @Column(columnDefinition = "TEXT")
    private String shortDescriptionRu;

    @Size(max = 500)
    @Column(columnDefinition = "TEXT")
    private String shortDescriptionKk;

    @Size(max = 500)
    @Column(columnDefinition = "TEXT")
    private String shortDescriptionEn;

    @Column(nullable = false)
    private Integer viewCount = 0;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NewsType newsType = NewsType.NEWS;

    @OneToMany(mappedBy = "news", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JsonIgnore
    private List<NewsImage> additionalImages = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (publishedDate == null) {
            publishedDate = LocalDateTime.now();
        }
        if (newsType == null) {
            newsType = NewsType.NEWS;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void incrementViewCount() {
        this.viewCount++;
    }
}