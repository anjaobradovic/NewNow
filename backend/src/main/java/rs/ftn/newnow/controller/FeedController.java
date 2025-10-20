package rs.ftn.newnow.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.ftn.newnow.dto.EventBasicDTO;
import rs.ftn.newnow.dto.LocationDTO;
import rs.ftn.newnow.dto.ReviewDetailsDTO;
import rs.ftn.newnow.service.FeedService;

import java.util.List;

@RestController
@RequestMapping("/api/feed")
@RequiredArgsConstructor
public class FeedController {

    private final FeedService feedService;

    @GetMapping("/today-events")
    public ResponseEntity<List<EventBasicDTO>> getTodayEvents() {
        List<EventBasicDTO> events = feedService.getTodayEvents();
        return ResponseEntity.ok(events);
    }

    @GetMapping("/popular-locations")
    public ResponseEntity<List<LocationDTO>> getPopularLocations(
            @RequestParam(defaultValue = "10") int limit) {
        List<LocationDTO> locations = feedService.getPopularLocations(limit);
        return ResponseEntity.ok(locations);
    }

    @GetMapping("/popular-location-latest-reviews")
    public ResponseEntity<List<ReviewDetailsDTO>> getPopularLocationLatestReviews() {
        List<ReviewDetailsDTO> reviews = feedService.getPopularLocationLatestReviews();
        return ResponseEntity.ok(reviews);
    }
}
