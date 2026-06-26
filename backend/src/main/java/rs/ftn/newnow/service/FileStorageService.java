package rs.ftn.newnow.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import rs.ftn.newnow.exception.FileSizeExceededException;
import rs.ftn.newnow.storage.ObjectStorageService;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileStorageService {

    private static final long MAX_FILE_SIZE = 15 * 1024 * 1024;

    private final ObjectStorageService objectStorage;

    /**
     * Saves an image to MinIO. The returned string is the public path that other
     * subsystems (DB, frontend) treat as the image URL. We deliberately keep the
     * "/uploads/{dir}/{file}" shape so existing rows / frontend URL building still work.
     * The MinIO object key is the same string without the leading slash.
     */
    public String saveImage(MultipartFile file, String directory) throws IOException {
        validateImageFile(file);

        String filename = generateUniqueFilename(file);
        String objectKey = directory + "/" + filename;

        try (InputStream in = file.getInputStream()) {
            objectStorage.putObject(objectKey, in, file.getSize(), file.getContentType());
        }

        String publicPath = "/uploads/" + objectKey;
        log.info("Saved image to MinIO key={}", objectKey);
        return publicPath;
    }

    public void deleteImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return;
        }
        String objectKey = imageUrl.startsWith("/uploads/") ? imageUrl.substring("/uploads/".length()) : imageUrl;
        objectStorage.remove(objectKey);
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
