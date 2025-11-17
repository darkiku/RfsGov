package kz.gov.rfs.service;

import kz.gov.rfs.entity.News;
import kz.gov.rfs.entity.NewsType;
import kz.gov.rfs.repository.NewsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NewsService {
    private final NewsRepository newsRepository;

    @Transactional(readOnly = true)
    public Page<News> getAllActiveNews(Pageable pageable) {
        return newsRepository.findByIsActiveTrueOrderByPublishedDateDesc(pageable);
    }

    @Transactional(readOnly = true)
    public Page<News> getAllActiveNewsByType(NewsType newsType, Pageable pageable) {
        // ИСПРАВЛЕНО: убран лишний параметр true
        return newsRepository.findByIsActiveTrueAndNewsTypeOrderByPublishedDateDesc(newsType, pageable);
    }

    @Transactional(readOnly = true)
    public News getNewsById(Long id) {
        News news = newsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("News not found with id: " + id));

        // Увеличиваем счётчик просмотров
        news.incrementViewCount();
        newsRepository.save(news);

        return news;
    }

    @Transactional(readOnly = true)
    public List<News> getLatestNews() {
        return newsRepository.findTop5ByIsActiveTrueOrderByPublishedDateDesc();
    }

    @Transactional(readOnly = true)
    public List<News> getLatestNewsByType(NewsType newsType) {
        return newsRepository.findTop5ByIsActiveTrueAndNewsTypeOrderByPublishedDateDesc(newsType);
    }

    @Transactional(readOnly = true)
    public Page<News> searchNews(String keyword, Pageable pageable) {
        return newsRepository.searchNews(keyword, pageable);
    }

    @Transactional(readOnly = true)
    public Page<News> searchNewsByType(String keyword, NewsType newsType, Pageable pageable) {
        return newsRepository.searchNewsByType(keyword, newsType, pageable);
    }

    @Transactional
    public News createNews(News news) {
        if (news.getPublishedDate() == null) {
            news.setPublishedDate(LocalDateTime.now());
        }
        news.setIsActive(true);
        if (news.getNewsType() == null) {
            news.setNewsType(NewsType.NEWS);
        }
        if (news.getViewCount() == null) {
            news.setViewCount(0);
        }
        return newsRepository.save(news);
    }

    @Transactional
    public News updateNews(Long id, News newsDetails) {
        News news = newsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("News not found with id: " + id));

        news.setTitleRu(newsDetails.getTitleRu());
        news.setTitleKk(newsDetails.getTitleKk());
        news.setTitleEn(newsDetails.getTitleEn());
        news.setContentRu(newsDetails.getContentRu());
        news.setContentKk(newsDetails.getContentKk());
        news.setContentEn(newsDetails.getContentEn());
        news.setShortDescriptionRu(newsDetails.getShortDescriptionRu());
        news.setShortDescriptionKk(newsDetails.getShortDescriptionKk());
        news.setShortDescriptionEn(newsDetails.getShortDescriptionEn());
        news.setImageUrl(newsDetails.getImageUrl());
        news.setAuthor(newsDetails.getAuthor());

        if (newsDetails.getNewsType() != null) {
            news.setNewsType(newsDetails.getNewsType());
        }

        return newsRepository.save(news);
    }

    @Transactional
    public void deleteNews(Long id) {
        News news = newsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("News not found with id: " + id));
        news.setIsActive(false);
        newsRepository.save(news);
    }

    @Transactional(readOnly = true)
    public Long getNewsCount() {
        return newsRepository.countByIsActive(true);
    }
}