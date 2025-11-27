package kz.gov.rfs.repository;

import kz.gov.rfs.entity.News;
import kz.gov.rfs.entity.NewsType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface NewsRepository extends JpaRepository<News, Long> {

    // Базовые запросы
    Page<News> findByIsActiveTrueOrderByPublishedDateDesc(Pageable pageable);
    Page<News> findByIsActiveTrueAndNewsTypeOrderByPublishedDateDesc(NewsType newsType, Pageable pageable);
    List<News> findTop5ByIsActiveTrueOrderByPublishedDateDesc();
    List<News> findTop5ByIsActiveTrueAndNewsTypeOrderByPublishedDateDesc(NewsType newsType);
    List<News> findTopByIsActiveTrueOrderByViewCountDesc(Pageable pageable);
    Long countByIsActive(Boolean isActive);

    @Query("SELECT n FROM News n WHERE n.isActive = true AND " +
            "(LOWER(n.titleRu) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(n.titleKk) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(n.titleEn) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(n.contentRu) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(n.contentKk) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(n.contentEn) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(n.shortDescriptionRu) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(n.shortDescriptionKk) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(n.shortDescriptionEn) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "ORDER BY n.publishedDate DESC")
    Page<News> searchNews(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT n FROM News n WHERE n.isActive = true AND n.newsType = :newsType AND " +
            "(LOWER(n.titleRu) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(n.titleKk) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(n.titleEn) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(n.contentRu) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(n.contentKk) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(n.contentEn) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(n.shortDescriptionRu) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(n.shortDescriptionKk) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(n.shortDescriptionEn) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "ORDER BY n.publishedDate DESC")
    Page<News> searchNewsByType(@Param("keyword") String keyword, @Param("newsType") NewsType newsType, Pageable pageable);
}