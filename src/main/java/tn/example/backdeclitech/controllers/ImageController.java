package tn.example.backdeclitech.controllers;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/uploads/news")
public class ImageController {

    private static final String UPLOAD_DIR = "uploads/news/";

    @GetMapping("/{filename:.+}")
    public ResponseEntity<Resource> getImage(@PathVariable String filename) {
        try {
            System.out.println("🖼️ Requête image: " + filename);

            Path filePath = Paths.get(UPLOAD_DIR + filename);
            File file = filePath.toFile();

            System.out.println("📂 Chemin complet: " + file.getAbsolutePath());
            System.out.println("📋 Existe: " + file.exists());

            if (!file.exists()) {
                System.err.println("❌ Fichier introuvable!");
                return ResponseEntity.notFound().build();
            }

            Resource resource = new FileSystemResource(file);

            // Déterminer le type de contenu
            String contentType = Files.probeContentType(filePath);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            System.out.println("✅ Fichier trouvé! Type: " + contentType);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .body(resource);

        } catch (Exception e) {
            System.err.println("❌ Erreur: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}