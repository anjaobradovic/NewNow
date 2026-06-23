package rs.ftn.newnow.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import rs.ftn.newnow.dto.MessageResponse;
import rs.ftn.newnow.search.PlaceSearchCriteria;
import rs.ftn.newnow.search.PlaceSearchService;
import rs.ftn.newnow.search.dto.PlaceSearchPageResponse;

@RestController
@RequestMapping("/api/search/places")
@RequiredArgsConstructor
@Slf4j
public class PlaceSearchController {

    private final PlaceSearchService placeSearchService;

    @GetMapping
    public ResponseEntity<?> search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String pdf,
            @RequestParam(required = false) Integer reviewsFrom,
            @RequestParam(required = false) Integer reviewsTo,
            @RequestParam(required = false) Double avgPerformanceFrom,
            @RequestParam(required = false) Double avgPerformanceTo,
            @RequestParam(required = false) Double avgSoundAndLightingFrom,
            @RequestParam(required = false) Double avgSoundAndLightingTo,
            @RequestParam(required = false) Double avgVenueFrom,
            @RequestParam(required = false) Double avgVenueTo,
            @RequestParam(required = false) Double avgOverallImpressionFrom,
            @RequestParam(required = false) Double avgOverallImpressionTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        String bad = validate(
                "reviews",     reviewsFrom == null ? null : reviewsFrom.doubleValue(), reviewsTo == null ? null : reviewsTo.doubleValue());
        if (bad == null) bad = validate("avgPerformance",        avgPerformanceFrom,        avgPerformanceTo);
        if (bad == null) bad = validate("avgSoundAndLighting",   avgSoundAndLightingFrom,   avgSoundAndLightingTo);
        if (bad == null) bad = validate("avgVenue",              avgVenueFrom,              avgVenueTo);
        if (bad == null) bad = validate("avgOverallImpression",  avgOverallImpressionFrom,  avgOverallImpressionTo);
        if (bad != null) return ResponseEntity.badRequest().body(new MessageResponse(bad));

        try {
            PlaceSearchCriteria criteria = PlaceSearchCriteria.builder()
                    .name(name)
                    .description(description)
                    .pdf(pdf)
                    .reviewsFrom(reviewsFrom)
                    .reviewsTo(reviewsTo)
                    .avgPerformanceFrom(avgPerformanceFrom)
                    .avgPerformanceTo(avgPerformanceTo)
                    .avgSoundAndLightingFrom(avgSoundAndLightingFrom)
                    .avgSoundAndLightingTo(avgSoundAndLightingTo)
                    .avgVenueFrom(avgVenueFrom)
                    .avgVenueTo(avgVenueTo)
                    .avgOverallImpressionFrom(avgOverallImpressionFrom)
                    .avgOverallImpressionTo(avgOverallImpressionTo)
                    .page(page)
                    .size(size)
                    .build();
            PlaceSearchPageResponse result = placeSearchService.search(criteria);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Place search failed", e);
            return ResponseEntity.internalServerError().body(new MessageResponse("Search failed"));
        }
    }

    private static String validate(String field, Double from, Double to) {
        if (from != null && to != null && from > to) {
            return field + "From must be <= " + field + "To";
        }
        return null;
    }
}
