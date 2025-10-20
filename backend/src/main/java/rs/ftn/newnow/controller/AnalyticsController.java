package rs.ftn.newnow.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import rs.ftn.newnow.dto.*;
import rs.ftn.newnow.model.User;
import rs.ftn.newnow.repository.UserRepository;
import rs.ftn.newnow.service.AnalyticsService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
@RequiredArgsConstructor
@Slf4j
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final UserRepository userRepository;

    @GetMapping("/locations/{id}/summary")
    @PreAuthorize("hasRole('ADMIN') or @analyticsService.isManagerOfLocation(#id, authentication.name)")
    public ResponseEntity<?> getLocationSummary(
            @PathVariable Long id,
            @RequestParam(defaultValue = "monthly") String period,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User currentUser = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            
            LocationSummaryDTO summary = analyticsService.getLocationSummary(id, period, startDate, endDate, currentUser);
            return ResponseEntity.ok(summary);
        } catch (IllegalArgumentException e) {
            log.error("Validation error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Error fetching location summary", e);
            return ResponseEntity.internalServerError()
                    .body(new MessageResponse("Failed to fetch location summary"));
        }
    }

    @GetMapping("/locations/{id}/events/counts")
    @PreAuthorize("hasRole('ADMIN') or @analyticsService.isManagerOfLocation(#id, authentication.name)")
    public ResponseEntity<?> getLocationEventCounts(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User currentUser = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            
            EventCountsDTO counts = analyticsService.getLocationEventCounts(id, currentUser);
            return ResponseEntity.ok(counts);
        } catch (IllegalArgumentException e) {
            log.error("Validation error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Error fetching event counts", e);
            return ResponseEntity.internalServerError()
                    .body(new MessageResponse("Failed to fetch event counts"));
        }
    }

    @GetMapping("/locations/{id}/ratings/top")
    @PreAuthorize("hasRole('ADMIN') or @analyticsService.isManagerOfLocation(#id, authentication.name)")
    public ResponseEntity<?> getTopRatings(
            @PathVariable Long id,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "desc") String direction,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User currentUser = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            
            TopRatingsDTO topRatings = analyticsService.getTopRatings(id, limit, direction, currentUser);
            return ResponseEntity.ok(topRatings);
        } catch (IllegalArgumentException e) {
            log.error("Validation error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Error fetching top ratings", e);
            return ResponseEntity.internalServerError()
                    .body(new MessageResponse("Failed to fetch top ratings"));
        }
    }

    @GetMapping("/locations/{id}/reviews/latest")
    @PreAuthorize("hasRole('ADMIN') or @analyticsService.isManagerOfLocation(#id, authentication.name)")
    public ResponseEntity<?> getLatestReviews(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User currentUser = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            
            List<ReviewDTO> reviews = analyticsService.getLatestReviews(id, currentUser);
            return ResponseEntity.ok(reviews);
        } catch (IllegalArgumentException e) {
            log.error("Validation error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Error fetching latest reviews", e);
            return ResponseEntity.internalServerError()
                    .body(new MessageResponse("Failed to fetch latest reviews"));
        }
    }
}
