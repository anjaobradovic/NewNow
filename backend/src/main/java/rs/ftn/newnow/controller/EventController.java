package rs.ftn.newnow.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import rs.ftn.newnow.dto.CreateEventDTO;
import rs.ftn.newnow.dto.EventDTO;
import rs.ftn.newnow.dto.MessageResponse;
import rs.ftn.newnow.dto.UpdateEventDTO;
import rs.ftn.newnow.exception.FileSizeExceededException;
import rs.ftn.newnow.model.User;
import rs.ftn.newnow.repository.UserRepository;
import rs.ftn.newnow.service.EventService;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
@RequiredArgsConstructor
@Slf4j
public class EventController {

    private final EventService eventService;
    private final UserRepository userRepository;

    @GetMapping("/events")
    public ResponseEntity<?> searchEvents(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Long locationId,
            @RequestParam(required = false) String address,
            @RequestParam(required = false) Double priceMin,
            @RequestParam(required = false) Double priceMax,
            @RequestParam(required = false) Boolean isFree,
            @RequestParam(required = false) Boolean isRegular,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            log.info("Searching events with filters");
            Page<EventDTO> events = eventService.searchEvents(
                    type, locationId, address, priceMin, priceMax, isFree, isRegular, date, page, size);
            return ResponseEntity.ok(events);
        } catch (Exception e) {
            log.error("Error searching events", e);
            return ResponseEntity.internalServerError()
                    .body(new MessageResponse("Failed to search events"));
        }
    }

    @GetMapping("/events/today")
    public ResponseEntity<?> getTodayEvents() {
        try {
            List<EventDTO> events = eventService.getTodayEvents();
            return ResponseEntity.ok(events);
        } catch (Exception e) {
            log.error("Error fetching today's events", e);
            return ResponseEntity.internalServerError()
                    .body(new MessageResponse("Failed to fetch today's events"));
        }
    }

    @GetMapping("/events/{id}")
    public ResponseEntity<?> getEventById(@PathVariable Long id) {
        try {
            log.info("Fetching event by ID: {}", id);
            EventDTO event = eventService.getEventById(id);
            return ResponseEntity.ok(event);
        } catch (IllegalArgumentException e) {
            log.error("Validation error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Error fetching event", e);
            return ResponseEntity.internalServerError()
                    .body(new MessageResponse("Failed to fetch event"));
        }
    }

    @PostMapping(value = "/locations/{locationId}/events", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<?> createEvent(
            @PathVariable Long locationId,
            @RequestParam("name") String name,
            @RequestParam("address") String address,
            @RequestParam("type") String type,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(value = "price", defaultValue = "0.0") Double price,
            @RequestParam(value = "recurrent", defaultValue = "false") Boolean recurrent,
            @RequestParam("image") MultipartFile image,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            log.info("Creating event for location ID: {}", locationId);
            
            User currentUser = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            
            CreateEventDTO createEventDTO = new CreateEventDTO();
            createEventDTO.setName(name);
            createEventDTO.setAddress(address);
            createEventDTO.setType(type);
            createEventDTO.setDate(date);
            createEventDTO.setPrice(price);
            createEventDTO.setRecurrent(recurrent);
            
            EventDTO event = eventService.createEvent(locationId, createEventDTO, image, currentUser);
            return ResponseEntity.ok(event);
        } catch (FileSizeExceededException e) {
            log.error("File size exceeded: {}", e.getMessage());
            return ResponseEntity.status(413).body(new MessageResponse(e.getMessage()));
        } catch (IllegalArgumentException e) {
            log.error("Validation error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        } catch (IOException e) {
            log.error("Error saving image", e);
            return ResponseEntity.internalServerError()
                    .body(new MessageResponse("Failed to save image"));
        } catch (Exception e) {
            log.error("Error creating event", e);
            return ResponseEntity.internalServerError()
                    .body(new MessageResponse("Failed to create event"));
        }
    }

    @PutMapping("/events/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<?> updateEvent(
            @PathVariable Long id,
            @Validated @RequestBody UpdateEventDTO updateEventDTO,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            log.info("Updating event ID: {}", id);
            
            User currentUser = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            
            EventDTO event = eventService.updateEvent(id, updateEventDTO, currentUser);
            return ResponseEntity.ok(event);
        } catch (IllegalArgumentException e) {
            log.error("Validation error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Error updating event", e);
            return ResponseEntity.internalServerError()
                    .body(new MessageResponse("Failed to update event"));
        }
    }

    @DeleteMapping("/events/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<?> deleteEvent(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            log.info("Deleting event ID: {}", id);
            
            User currentUser = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            
            eventService.deleteEvent(id, currentUser);
            return ResponseEntity.ok(new MessageResponse("Event deleted successfully"));
        } catch (IllegalArgumentException e) {
            log.error("Validation error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Error deleting event", e);
            return ResponseEntity.internalServerError()
                    .body(new MessageResponse("Failed to delete event"));
        }
    }

    @PutMapping(value = "/events/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<?> updateEventImage(
            @PathVariable Long id,
            @RequestParam("image") MultipartFile image,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            log.info("Updating image for event ID: {}", id);
            
            User currentUser = userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            
            EventDTO updatedEvent = eventService.updateEventImage(id, image, currentUser);
            return ResponseEntity.ok(updatedEvent);
        } catch (FileSizeExceededException e) {
            log.error("File size exceeded: {}", e.getMessage());
            return ResponseEntity.status(413).body(new MessageResponse(e.getMessage()));
        } catch (IllegalArgumentException e) {
            log.error("Validation error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        } catch (IOException e) {
            log.error("Error saving image", e);
            return ResponseEntity.internalServerError()
                    .body(new MessageResponse("Failed to save image"));
        } catch (Exception e) {
            log.error("Error updating event image", e);
            return ResponseEntity.internalServerError()
                    .body(new MessageResponse("Failed to update event image"));
        }
    }

    @GetMapping("/events/{id}/occurrences/count")
    public ResponseEntity<?> countEventOccurrences(
            @PathVariable Long id,
            @RequestParam("untilDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate untilDate) {
        try {
            log.info("Counting occurrences for event ID: {} until date: {}", id, untilDate);
            Long count = eventService.countEventOccurrences(id, untilDate);
            return ResponseEntity.ok(Map.of("count", count));
        } catch (IllegalArgumentException e) {
            log.error("Validation error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Error counting event occurrences", e);
            return ResponseEntity.internalServerError()
                    .body(new MessageResponse("Failed to count event occurrences"));
        }
    }
}
