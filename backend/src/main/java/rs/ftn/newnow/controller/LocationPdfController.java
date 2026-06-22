package rs.ftn.newnow.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import rs.ftn.newnow.dto.MessageResponse;
import rs.ftn.newnow.repository.LocationRepository;
import rs.ftn.newnow.search.PdfTextExtractor;
import rs.ftn.newnow.search.SearchIndexService;
import rs.ftn.newnow.service.LocationService;
import rs.ftn.newnow.storage.ObjectStorageService;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

@RestController
@RequestMapping("/api/locations/{id}/description-pdf")
@RequiredArgsConstructor
@Slf4j
public class LocationPdfController {

    private final ObjectStorageService objectStorage;
    private final PdfTextExtractor pdfTextExtractor;
    private final SearchIndexService searchIndexService;
    private final LocationRepository locationRepository;
    private final LocationService locationService;

    @Value("${newnow.pdf.max-size-mb}")
    private int maxSizeMb;

    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN') or @locationService.isManagerOfLocation(#id, authentication.name)")
    public ResponseEntity<?> upload(@PathVariable Long id,
                                    @RequestParam("file") MultipartFile file,
                                    @AuthenticationPrincipal UserDetails userDetails) {
        if (!locationRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(new MessageResponse("PDF file is required"));
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.equals(MediaType.APPLICATION_PDF_VALUE)) {
            return ResponseEntity.badRequest().body(new MessageResponse("File must be application/pdf"));
        }
        long maxBytes = (long) maxSizeMb * 1024L * 1024L;
        if (file.getSize() > maxBytes) {
            return ResponseEntity.status(413).body(new MessageResponse("PDF exceeds " + maxSizeMb + " MB limit"));
        }

        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(new MessageResponse("Failed to read upload"));
        }

        String key = SearchIndexService.pdfKeyForLocation(id);
        try {
            objectStorage.putObject(key, new ByteArrayInputStream(bytes), bytes.length, MediaType.APPLICATION_PDF_VALUE);
        } catch (IOException e) {
            log.error("Failed to store PDF for location {}: {}", id, e.getMessage());
            return ResponseEntity.internalServerError().body(new MessageResponse("Failed to store PDF"));
        }

        String text;
        try {
            text = pdfTextExtractor.extract(new ByteArrayInputStream(bytes));
        } catch (IOException e) {
            log.error("Failed to extract PDF text for location {}: {}", id, e.getMessage());
            text = "";
        }

        searchIndexService.reindexAfterPdfUpload(id, text);

        return ResponseEntity.ok(Map.of(
                "locationId", id,
                "size", bytes.length,
                "characters", text.length()
        ));
    }

    @GetMapping
    public ResponseEntity<?> download(@PathVariable Long id) {
        if (!locationRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        String key = SearchIndexService.pdfKeyForLocation(id);
        if (!objectStorage.exists(key)) {
            return ResponseEntity.notFound().build();
        }
        try {
            InputStream in = objectStorage.getObject(key);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(
                    org.springframework.http.ContentDisposition.inline()
                            .filename("location-" + id + "-description.pdf")
                            .build()
            );
            return ResponseEntity.ok().headers(headers).body(new InputStreamResource(in));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping
    @PreAuthorize("hasRole('ADMIN') or @locationService.isManagerOfLocation(#id, authentication.name)")
    public ResponseEntity<?> delete(@PathVariable Long id,
                                    @AuthenticationPrincipal UserDetails userDetails) {
        if (!locationRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        String key = SearchIndexService.pdfKeyForLocation(id);
        objectStorage.remove(key);
        searchIndexService.reindexAfterPdfDelete(id);
        return ResponseEntity.ok(new MessageResponse("PDF removed"));
    }
}
