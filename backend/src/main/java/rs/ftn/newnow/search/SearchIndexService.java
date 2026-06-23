package rs.ftn.newnow.search;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import rs.ftn.newnow.model.Location;
import rs.ftn.newnow.repository.LocationRepository;
import rs.ftn.newnow.repository.ReviewRepository;
import rs.ftn.newnow.search.index.LocationIndex;
import rs.ftn.newnow.search.repository.LocationIndexRepository;
import rs.ftn.newnow.storage.ObjectStorageService;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchIndexService {

    private final LocationRepository locationRepository;
    private final ReviewRepository reviewRepository;
    private final LocationIndexRepository locationIndexRepository;
    private final ElasticsearchOperations elasticsearchOperations;
    private final ObjectStorageService objectStorage;
    private final PdfTextExtractor pdfTextExtractor;

    /** Pdf object key convention: places/{id}/description.pdf (no DB column needed). */
    public static String pdfKeyForLocation(Long id) {
        return "places/" + id + "/description.pdf";
    }

    /**
     * Schedule a reindex of one place to run AFTER the current JPA transaction commits.
     * If no transaction is active, runs immediately.
     */
    public void reindexAfterCommit(Long locationId) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    reindexOne(locationId);
                }
            });
        } else {
            reindexOne(locationId);
        }
    }

    /** Index one place. Soft-deleted locations are removed from the index. */
    public void reindexOne(Long locationId) {
        try {
            Location location = locationRepository.findById(locationId).orElse(null);
            if (location == null || Boolean.TRUE.equals(location.getDeleted())) {
                locationIndexRepository.deleteById(String.valueOf(locationId));
                return;
            }
            LocationIndex doc = buildDocument(location);
            locationIndexRepository.save(doc);
        } catch (Exception e) {
            log.error("Failed to reindex location id={}: {}", locationId, e.getMessage());
        }
    }

    /** Drop + recreate the index, then index every non-deleted place. */
    public int bulkReindex() {
        try {
            IndexOperations ops = elasticsearchOperations.indexOps(LocationIndex.class);
            if (ops.exists()) {
                ops.delete();
            }
            ops.create();
            ops.putMapping();
        } catch (Exception e) {
            log.error("Failed to recreate index 'places': {}", e.getMessage());
        }

        List<Location> all = locationRepository.findAll();
        int indexed = 0;
        for (Location loc : all) {
            if (Boolean.TRUE.equals(loc.getDeleted())) continue;
            try {
                locationIndexRepository.save(buildDocument(loc));
                indexed++;
            } catch (Exception e) {
                log.error("Failed to index location id={}: {}", loc.getId(), e.getMessage());
            }
        }
        log.info("Bulk reindex complete: {} places indexed", indexed);
        return indexed;
    }

    private LocationIndex buildDocument(Location loc) {
        long reviewCount = countReviews(loc.getId());
        CategoryAverages avgs = CategoryAverages.compute(
                reviewRepository.findRatingSourceForLocation(loc.getId())
        );

        String pdfKey = pdfKeyForLocation(loc.getId());
        String pdfText = null;
        Instant pdfUploadedAt = null;
        String storedKey = null;
        if (objectStorage.exists(pdfKey)) {
            try (InputStream in = objectStorage.getObject(pdfKey)) {
                pdfText = pdfTextExtractor.extract(in);
                storedKey = pdfKey;
                pdfUploadedAt = Instant.now();
            } catch (IOException e) {
                log.warn("Could not re-extract PDF for location id={}: {}", loc.getId(), e.getMessage());
            }
        }

        return LocationIndex.builder()
                .id(String.valueOf(loc.getId()))
                .name(loc.getName())
                .description(loc.getDescription())
                .pdfDescription(pdfText)
                .pdfObjectKey(storedKey)
                .pdfUploadedAt(pdfUploadedAt)
                .reviewCount((int) reviewCount)
                .address(loc.getAddress())
                .type(loc.getType())
                .totalRating(loc.getTotalRating())
                .avgPerformance(avgs.getAvgPerformance())
                .avgSoundAndLighting(avgs.getAvgSoundAndLighting())
                .avgVenue(avgs.getAvgVenue())
                .avgOverallImpression(avgs.getAvgOverallImpression())
                .imageUrl(loc.getImageUrl())
                .deleted(Boolean.TRUE.equals(loc.getDeleted()))
                .build();
    }

    private long countReviews(Long locationId) {
        Long c = reviewRepository.countByLocationAndNotDeleted(locationId);
        return c == null ? 0L : c;
    }

    /**
     * Reindex a place after a PDF was just uploaded. Extracts text from the provided bytes
     * so we don't have to fetch from MinIO and round-trip.
     */
    public void reindexAfterPdfUpload(Long locationId, String pdfText) {
        try {
            Location location = locationRepository.findById(locationId).orElse(null);
            if (location == null) return;
            LocationIndex existing = locationIndexRepository.findById(String.valueOf(locationId))
                    .orElse(buildDocument(location));
            existing.setPdfDescription(pdfText);
            existing.setPdfObjectKey(pdfKeyForLocation(locationId));
            existing.setPdfUploadedAt(Instant.now());
            locationIndexRepository.save(existing);
        } catch (Exception e) {
            log.error("Failed to reindex location id={} after PDF upload: {}", locationId, e.getMessage());
        }
    }

    public void reindexAfterPdfDelete(Long locationId) {
        try {
            locationIndexRepository.findById(String.valueOf(locationId)).ifPresent(doc -> {
                doc.setPdfDescription(null);
                doc.setPdfObjectKey(null);
                doc.setPdfUploadedAt(null);
                locationIndexRepository.save(doc);
            });
        } catch (Exception e) {
            log.error("Failed to reindex location id={} after PDF delete: {}", locationId, e.getMessage());
        }
    }
}
