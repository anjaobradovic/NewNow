package rs.ftn.newnow.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import rs.ftn.newnow.dto.MessageResponse;
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
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        if (reviewsFrom != null && reviewsTo != null && reviewsFrom > reviewsTo) {
            return ResponseEntity.badRequest().body(new MessageResponse("reviewsFrom must be <= reviewsTo"));
        }
        try {
            PlaceSearchPageResponse result = placeSearchService.search(name, description, pdf, reviewsFrom, reviewsTo, page, size);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Place search failed", e);
            return ResponseEntity.internalServerError().body(new MessageResponse("Search failed"));
        }
    }
}
