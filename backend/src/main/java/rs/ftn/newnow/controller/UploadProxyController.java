package rs.ftn.newnow.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerMapping;
import jakarta.servlet.http.HttpServletRequest;
import rs.ftn.newnow.storage.ObjectStorageService;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

@RestController
@RequestMapping("/uploads")
@RequiredArgsConstructor
@Slf4j
public class UploadProxyController {

    private final ObjectStorageService objectStorage;

    @GetMapping("/**")
    public ResponseEntity<?> serve(HttpServletRequest request) {
        String full = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        if (full == null) {
            full = request.getRequestURI().substring("/uploads/".length());
        } else if (full.startsWith("/uploads/")) {
            full = full.substring("/uploads/".length());
        }
        if (full.isBlank()) {
            return ResponseEntity.notFound().build();
        }
        if (!objectStorage.exists(full)) {
            return ResponseEntity.notFound().build();
        }
        try {
            InputStream in = objectStorage.getObject(full);
            MediaType type = guessType(full);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(type);
            headers.setCacheControl("public, max-age=3600");
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(new InputStreamResource(in));
        } catch (IOException e) {
            log.error("Failed to stream object {}: {}", full, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    private MediaType guessType(String key) {
        String lower = key.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".png")) return MediaType.IMAGE_PNG;
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return MediaType.IMAGE_JPEG;
        if (lower.endsWith(".gif")) return MediaType.IMAGE_GIF;
        if (lower.endsWith(".webp")) return MediaType.parseMediaType("image/webp");
        if (lower.endsWith(".svg")) return MediaType.parseMediaType("image/svg+xml");
        if (lower.endsWith(".pdf")) return MediaType.APPLICATION_PDF;
        return MediaType.APPLICATION_OCTET_STREAM;
    }
}
