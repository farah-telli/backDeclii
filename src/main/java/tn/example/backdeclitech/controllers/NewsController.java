package tn.example.backdeclitech.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tn.example.backdeclitech.dto.NewsDTO;
import tn.example.backdeclitech.entities.News;
import tn.example.backdeclitech.services.NewsService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.File;

@RestController
@RequestMapping("/api/news")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4300")
public class NewsController {

    private final NewsService newsService;

    @Value("${app.upload-dir:uploads/news}")
    private String uploadDir;

    @GetMapping
    public ResponseEntity<List<NewsDTO>> getAllNews() {
        List<NewsDTO> newsList = newsService.getAllNews();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(newsList);
    }

    @GetMapping("/{id}")
    public ResponseEntity<NewsDTO> getNewsById(@PathVariable Long id) {
        return newsService.getNewsDTOById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<NewsDTO> createNews(
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestParam("type") String type,
            @RequestParam(value = "image", required = false) MultipartFile image
    ) {
        News news = new News();
        news.setTitle(title);
        news.setContent(content);
        news.setType(type);
        news.setCreatedAt(LocalDateTime.now());

        if (image != null && !image.isEmpty()) {
            try {
                String fileName = saveImage(image);
                news.setImageUrl(fileName);
                System.out.println("✅ Image sauvegardée: " + fileName);
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("❌ Erreur lors de la sauvegarde de l'image: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        }

        NewsDTO savedNews = newsService.createNews(news);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedNews);
    }

    @PreAuthorize("hasRole('SUPERADMIN')")
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<NewsDTO> updateNews(
            @PathVariable Long id,
            @RequestParam("title") String title,
            @RequestParam("content") String content,
            @RequestParam("type") String type,
            @RequestParam(value = "image", required = false) MultipartFile image
    ) {
        return newsService.getNewsById(id).map(existingNews -> {
            existingNews.setTitle(title);
            existingNews.setContent(content);
            existingNews.setType(type);

            if (image != null && !image.isEmpty()) {
                try {
                    if (existingNews.getImageUrl() != null) {
                        deleteImage(existingNews.getImageUrl());
                    }

                    String fileName = saveImage(image);
                    existingNews.setImageUrl(fileName);
                    System.out.println("✅ Nouvelle image sauvegardée: " + fileName);

                } catch (IOException e) {
                    e.printStackTrace();
                    System.err.println("❌ Erreur lors de la mise à jour de l'image: " + e.getMessage());
                }
            }

            NewsDTO updatedNews = newsService.updateNews(id, existingNews);
            return ResponseEntity.ok(updatedNews);
        }).orElse(ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasRole('SUPERADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNews(@PathVariable Long id) {
        newsService.getNewsById(id).ifPresent(news -> {
            if (news.getImageUrl() != null) {
                deleteImage(news.getImageUrl());
            }
        });

        newsService.deleteNews(id);
        return ResponseEntity.noContent().build();
    }

    private String saveImage(MultipartFile image) throws IOException {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String originalFilename = image.getOriginalFilename();
        String fileName = timestamp + "_" + originalFilename;

        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        Path filePath = uploadPath.resolve(fileName);
        Files.write(filePath, image.getBytes());

        return fileName;
    }

    private void deleteImage(String fileName) {
        try {
            Path filePath = Paths.get(uploadDir, fileName);
            Files.deleteIfExists(filePath);
            System.out.println("🗑️ Image supprimée: " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("❌ Erreur lors de la suppression de l'image: " + e.getMessage());
        }
    }
    @GetMapping("/test-images/{filename}")
    public ResponseEntity<Resource> serveImage(@PathVariable String filename) {
        try {
            System.out.println("🖼️ Demande image: " + filename);

            Path filePath = Paths.get("uploads/news/" + filename);
            File file = filePath.toFile();

            System.out.println("📂 Chemin: " + file.getAbsolutePath());
            System.out.println("📋 Existe: " + file.exists());

            if (!file.exists()) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = new FileSystemResource(file);
            String contentType = Files.probeContentType(filePath);

            if (contentType == null) {
                contentType = "image/png";
            }

            System.out.println("✅ Image trouvée!");

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }
}