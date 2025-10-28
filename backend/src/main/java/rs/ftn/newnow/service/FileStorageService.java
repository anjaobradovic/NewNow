package rs.ftn.newnow.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import rs.ftn.newnow.exception.FileSizeExceededException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileStorageService {

    private static final long MAX_FILE_SIZE = 15 * 1024 * 1024;

    public String saveImage(MultipartFile file, String directory) throws IOException {
        validateImageFile(file);
        
        Path uploadPath = Paths.get("uploads", directory);
        Files.createDirectories(uploadPath);

        String filename = generateUniqueFilename(file);
        Path targetPath = uploadPath.resolve(filename);

        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        log.info("Saved image: {} to directory: {}", filename, directory);
        return String.format("/uploads/%s/%s", directory, filename);
    }

    public void deleteImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return;
        }

        try {
            Path filePath = Paths.get(imageUrl.replaceFirst("^/", ""));
            Files.deleteIfExists(filePath);
            log.info("Deleted image: {}", imageUrl);
        } catch (IOException e) {
            log.error("Failed to delete image: {}", imageUrl, e);
        }
    }

    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("File must be an image");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            double sizeMB = file.getSize() / (1024.0 * 1024.0);
            throw new FileSizeExceededException(
                String.format("Image size %.2f MB exceeds the maximum allowed size of 15 MB", sizeMB)
            );
        }
    }

    private String generateUniqueFilename(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : ".jpg";
        return UUID.randomUUID().toString() + extension;
    }
}
