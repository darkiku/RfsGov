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

    // Все активные новости
    Page<News> findByIsActiveTrueOrderByPublishedDateDesc(Pageable pageable);

    // Активные новости по типу - ИСПРАВЛЕНО!
    Page<News> findByIsActiveTrueAndNewsTypeOrderByPublishedDateDesc(NewsType newsType, Pageable pageable);

    // Последние 5 новостей (все типы)
    List<News> findTop5ByIsActiveTrueOrderByPublishedDateDesc();

    // Последние 5 новостей по типу
    List<News> findTop5ByIsActiveTrueAndNewsTypeOrderByPublishedDateDesc(NewsType newsType);

    // Топ по просмотрам
    List<News> findTopByIsActiveTrueOrderByViewCountDesc(Pageable pageable);

    // Количество активных новостей
    Long countByIsActive(Boolean isActive);

    // Поиск по всем полям (все типы)
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

    // Поиск по всем полям с типом
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