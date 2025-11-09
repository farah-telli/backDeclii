package tn.example.backdeclitech.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@Slf4j
public class FileStorageService {

    private final Path fileStorageLocation;

    public FileStorageService(@Value("${file.upload-dir:uploads/voice-messages}") String uploadDir) {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
            log.info("File storage directory created at: {}", this.fileStorageLocation);
        } catch (IOException ex) {
            throw new RuntimeException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    /**
     * Sauvegarder un fichier audio
     */
    public String storeVoiceMessage(MultipartFile file, Long senderId, Long receiverId) {
        // Valider le fichier
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Cannot store empty file");
        }

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());

        // Valider l'extension
        if (!isValidAudioFile(originalFilename)) {
            throw new IllegalArgumentException("Invalid audio file format. Supported: webm, mp3, wav, ogg");
        }

        try {
            // Générer un nom de fichier unique
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String uniqueId = UUID.randomUUID().toString().substring(0, 8);
            String extension = getFileExtension(originalFilename);
            String fileName = String.format("voice_%s_%s_%s_%s.%s",
                    senderId, receiverId, timestamp, uniqueId, extension);

            // Vérifier les caractères malveillants
            if (fileName.contains("..")) {
                throw new IllegalArgumentException("Filename contains invalid path sequence: " + fileName);
            }

            // Copier le fichier
            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            log.info("Voice message file stored: {}", fileName);
            return fileName;

        } catch (IOException ex) {
            log.error("Failed to store voice message file", ex);
            throw new RuntimeException("Could not store file. Please try again!", ex);
        }
    }

    /**
     * Charger un fichier audio
     */
    public Resource loadVoiceMessage(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("File not found: " + fileName);
            }
        } catch (MalformedURLException ex) {
            log.error("File not found: {}", fileName, ex);
            throw new RuntimeException("File not found: " + fileName, ex);
        }
    }

    /**
     * Supprimer un fichier audio
     */
    public void deleteVoiceMessage(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Files.deleteIfExists(filePath);
            log.info("Voice message file deleted: {}", fileName);
        } catch (IOException ex) {
            log.error("Failed to delete file: {}", fileName, ex);
        }
    }

    /**
     * Valider le type de fichier audio
     */
    private boolean isValidAudioFile(String filename) {
        String extension = getFileExtension(filename).toLowerCase();
        return extension.equals("webm") ||
                extension.equals("mp3") ||
                extension.equals("wav") ||
                extension.equals("ogg") ||
                extension.equals("m4a");
    }

    /**
     * Obtenir l'extension du fichier
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    /**
     * Obtenir le chemin du répertoire de stockage
     */
    public Path getFileStorageLocation() {
        return fileStorageLocation;
    }
}