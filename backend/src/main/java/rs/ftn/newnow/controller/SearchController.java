package rs.ftn.newnow.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.ftn.newnow.dto.EventDTO;
import rs.ftn.newnow.dto.LocationDTO;
import rs.ftn.newnow.dto.MessageResponse;
import rs.ftn.newnow.service.SearchService;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/search")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
@RequiredArgsConstructor
@Slf4j
public class SearchController {

    private final SearchService searchService;

    @GetMapping("/locations")
    public ResponseEntity<?> searchLocations(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String address,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<LocationDTO> locations = searchService.searchLocations(q, type, address, page, size);
            return ResponseEntity.ok(locations);
        } catch (Exception e) {
            log.error("Error searching locations", e);
            return ResponseEntity.internalServerError()
                    .body(new MessageResponse("Failed to search locations"));
        }
    }

    @GetMapping("/events")
    public ResponseEntity<?> searchEvents(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Long locationId,
            @RequestParam(required = false) String address,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Boolean past,
            @RequestParam(required = false) Boolean future,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<EventDTO> events = searchService.searchEvents(
                    type, locationId, address, minPrice, maxPrice, startDate, endDate, past, future, page, size);
            return ResponseEntity.ok(events);
        } catch (Exception e) {
            log.error("Error searching events", e);
            return ResponseEntity.internalServerError()
                    .body(new MessageResponse("Failed to search events"));
        }
    }
}
