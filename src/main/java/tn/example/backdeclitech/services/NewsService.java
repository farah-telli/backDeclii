package tn.example.backdeclitech.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tn.example.backdeclitech.dto.NewsDTO;
import tn.example.backdeclitech.entities.News;
import tn.example.backdeclitech.repositories.NewsRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class NewsService {

    @Autowired
    private NewsRepository newsRepository;

    @Value("${app.base-url:http://localhost:8089/declitech}")
    private String baseUrl;

    public List<NewsDTO> getAllNews() {
        return newsRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Optional<NewsDTO> getNewsDTOById(Long id) {
        return newsRepository.findById(id).map(this::convertToDTO);
    }

    public Optional<News> getNewsById(Long id) {
        return newsRepository.findById(id);
    }

    public NewsDTO createNews(News news) {
        News savedNews = newsRepository.save(news);
        return convertToDTO(savedNews);
    }

    public NewsDTO updateNews(Long id, News newsDetails) {
        News updatedNews = newsRepository.findById(id).map(news -> {
            news.setTitle(newsDetails.getTitle());
            news.setContent(newsDetails.getContent());
            news.setImageUrl(newsDetails.getImageUrl());
            news.setCreatedAt(newsDetails.getCreatedAt());
            news.setType(newsDetails.getType());
            news.setModule(newsDetails.getModule());
            return newsRepository.save(news);
        }).orElseThrow(() -> new RuntimeException("News not found with id " + id));

        return convertToDTO(updatedNews);
    }

    public void deleteNews(Long id) {
        newsRepository.deleteById(id);
    }

    private NewsDTO convertToDTO(News news) {
        NewsDTO dto = new NewsDTO();
        dto.setId(news.getId());
        dto.setTitle(news.getTitle());
        dto.setContent(news.getContent());
        dto.setCreatedAt(news.getCreatedAt());
        dto.setType(news.getType());

        if (news.getImageUrl() != null && !news.getImageUrl().isEmpty()) {
            dto.setImageUrl(baseUrl + "/uploads/news/" + news.getImageUrl());
        } else {
            dto.setImageUrl(null);
        }

        return dto;
    }
}