package rs.ftn.newnow.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import rs.ftn.newnow.dto.*;
import rs.ftn.newnow.exception.FileSizeExceededException;
import rs.ftn.newnow.service.LocationService;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
@Slf4j
public class LocationController {

    private final LocationService locationService;

    @GetMapping
    public ResponseEntity<LocationPageResponse> getLocations(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            LocationPageResponse response = locationService.getLocations(search, page, size);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error fetching locations", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<LocationDetailsDTO> getLocationDetails(@PathVariable Long id) {
        try {
            LocationDetailsDTO location = locationService.getLocationDetails(id);
            return ResponseEntity.ok(location);
        } catch (RuntimeException e) {
            log.error("Location not found: {}", id, e);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error fetching location details", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createLocation(
            @RequestParam String name,
            @RequestParam String address,
            @RequestParam String type,
            @RequestParam(required = false) String description,
            @RequestParam MultipartFile image) {
        try {
            CreateLocationDTO dto = new CreateLocationDTO(name, address, type, description);
            LocationDTO created = locationService.createLocation(dto, image);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (FileSizeExceededException e) {
            log.error("File size exceeded: {}", e.getMessage());
            return ResponseEntity.status(413).body(new MessageResponse(e.getMessage()));
        } catch (IllegalArgumentException e) {
            log.error("Invalid input for location creation", e);
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        } catch (IOException e) {
            log.error("Error saving image", e);
            return ResponseEntity.internalServerError().body(new MessageResponse("Failed to save image"));
        } catch (Exception e) {
            log.error("Error creating location", e);
            return ResponseEntity.internalServerError().body(new MessageResponse("Failed to create location"));
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LocationDTO> updateLocation(
            @PathVariable Long id,
            @Valid @RequestBody UpdateLocationDTO dto) {
        try {
            LocationDTO updated = locationService.updateLocation(id, dto);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            log.error("Location not found: {}", id, e);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error updating location", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @locationService.isManagerOfLocation(#id, authentication.name)")
    public ResponseEntity<LocationDTO> patchLocation(
            @PathVariable Long id,
            @RequestBody PatchLocationDTO dto,
            Authentication authentication) {
        try {
            LocationDTO updated = locationService.patchLocation(id, dto);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            log.error("Location not found: {}", id, e);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error patching location", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteLocation(@PathVariable Long id) {
        try {
            locationService.deleteLocation(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            log.error("Location not found: {}", id, e);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error deleting location", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN') or @locationService.isManagerOfLocation(#id, authentication.name)")
    public ResponseEntity<?> updateLocationImage(
            @PathVariable Long id,
            @RequestParam MultipartFile image) {
        try {
            LocationDTO updated = locationService.updateLocationImage(id, image);
            return ResponseEntity.ok(updated);
        } catch (FileSizeExceededException e) {
            log.error("File size exceeded: {}", e.getMessage());
            return ResponseEntity.status(413).body(new MessageResponse(e.getMessage()));
        } catch (IllegalArgumentException e) {
            log.error("Validation error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        } catch (RuntimeException e) {
            log.error("Location not found: {}", id, e);
            return ResponseEntity.notFound().build();
        } catch (IOException e) {
            log.error("Error saving image", e);
            return ResponseEntity.internalServerError().body(new MessageResponse("Failed to save image"));
        } catch (Exception e) {
            log.error("Error updating location image", e);
            return ResponseEntity.internalServerError().body(new MessageResponse("Failed to update location image"));
        }
    }

    @GetMapping("/{id}/events/upcoming")
    public ResponseEntity<Page<EventDTO>> getUpcomingEvents(
            @PathVariable Long id,
            @RequestParam(required = false) LocalDate dateFrom,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<EventDTO> events = locationService.getUpcomingEvents(id, dateFrom, page, size);
            return ResponseEntity.ok(events);
        } catch (RuntimeException e) {
            log.error("Location not found: {}", id, e);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error fetching upcoming events", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{id}/reviews")
    public ResponseEntity<Page<ReviewDTO>> getLocationReviews(
            @PathVariable Long id,
            @RequestParam(defaultValue = "date") String sort,
            @RequestParam(defaultValue = "desc") String order,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<ReviewDTO> reviews = locationService.getLocationReviews(id, sort, order, page, size);
            return ResponseEntity.ok(reviews);
        } catch (RuntimeException e) {
            log.error("Location not found: {}", id, e);
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error fetching location reviews", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/popular")
    public ResponseEntity<List<LocationDTO>> getPopularLocations(
            @RequestParam(defaultValue = "10") Integer limit) {
        try {
            List<LocationDTO> locations = locationService.getPopularLocations(limit);
            return ResponseEntity.ok(locations);
        } catch (Exception e) {
            log.error("Error fetching popular locations", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
