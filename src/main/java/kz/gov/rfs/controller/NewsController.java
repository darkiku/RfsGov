package kz.gov.rfs.controller;

import kz.gov.rfs.entity.News;
import kz.gov.rfs.entity.NewsType;
import kz.gov.rfs.entity.User;
import kz.gov.rfs.service.AuditLogService;
import kz.gov.rfs.service.NewsService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
public class NewsController {
    private final NewsService newsService;
    private final AuditLogService auditLogService;

    // Публичные эндпоинты (доступны всем)
    @GetMapping
    public ResponseEntity<Page<News>> getAllNews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(newsService.getAllActiveNews(pageable));
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<Page<News>> getNewsByType(
            @PathVariable String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        NewsType newsType = NewsType.valueOf(type.toUpperCase());
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(newsService.getAllActiveNewsByType(newsType, pageable));
    }

    @GetMapping("/latest")
    public ResponseEntity<List<News>> getLatestNews() {
        return ResponseEntity.ok(newsService.getLatestNews());
    }

    @GetMapping("/latest/{type}")
    public ResponseEntity<List<News>> getLatestNewsByType(@PathVariable String type) {
        NewsType newsType = NewsType.valueOf(type.toUpperCase());
        return ResponseEntity.ok(newsService.getLatestNewsByType(newsType));
    }

    @GetMapping("/{id}")
    public ResponseEntity<News> getNewsById(@PathVariable Long id) {
        return ResponseEntity.ok(newsService.getNewsById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<Page<News>> searchNews(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(newsService.searchNews(keyword, pageable));
    }

    @GetMapping("/search/{type}")
    public ResponseEntity<Page<News>> searchNewsByType(
            @PathVariable String type,
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        NewsType newsType = NewsType.valueOf(type.toUpperCase());
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(newsService.searchNewsByType(keyword, newsType, pageable));
    }

    // Защищенные эндпоинты (только для ADMIN и NEWS_MANAGER)
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'NEWS_MANAGER')")
    public ResponseEntity<News> createNews(
            @Valid @RequestBody News news,
            @AuthenticationPrincipal User user) {
        News created = newsService.createNews(news);
        auditLogService.log(user, "CREATE", "News", created.getId(),
                "Created news: " + news.getTitleRu());
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'NEWS_MANAGER')")
    public ResponseEntity<News> updateNews(
            @PathVariable Long id,
            @Valid @RequestBody News news,
            @AuthenticationPrincipal User user) {
        News updated = newsService.updateNews(id, news);
        auditLogService.log(user, "UPDATE", "News", id,
                "Updated news: " + news.getTitleRu());
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'NEWS_MANAGER')")
    public ResponseEntity<Void> deleteNews(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        newsService.deleteNews(id);
        auditLogService.log(user, "DELETE", "News", id, "Deleted news");
        return ResponseEntity.noContent().build();
    }
}