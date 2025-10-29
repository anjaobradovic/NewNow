package rs.ftn.newnow.service;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import rs.ftn.newnow.dto.CreateEventDTO;
import rs.ftn.newnow.dto.EventDTO;
import rs.ftn.newnow.dto.UpdateEventDTO;
import rs.ftn.newnow.model.Event;
import rs.ftn.newnow.model.Image;
import rs.ftn.newnow.model.Location;
import rs.ftn.newnow.model.User;
import rs.ftn.newnow.repository.EventRepository;
import rs.ftn.newnow.repository.ImageRepository;
import rs.ftn.newnow.repository.LocationRepository;
import rs.ftn.newnow.repository.ManagesRepository;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventService {

    private final EventRepository eventRepository;
    private final LocationRepository locationRepository;
    private final ManagesRepository managesRepository;
    private final ImageRepository imageRepository;
    private final FileStorageService fileStorageService;
    private final EntityManager entityManager;

    @Transactional(readOnly = true)
    public Page<EventDTO> searchEvents(String type, Long locationId, String address, 
                                       Double priceMin, Double priceMax, Boolean isFree, 
                                       Boolean isRegular, LocalDate date, int page, int size) {
        log.info("Searching events with filters");
        Pageable pageable = PageRequest.of(page, size, Sort.by("date").ascending());
        
        Page<Event> events = eventRepository.findByFilters(
                type, locationId, address, priceMin, priceMax, isFree, isRegular, date, pageable);
        
        return events.map(this::convertToDTO);
    }

    @Transactional(readOnly = true)
    public List<EventDTO> getTodayEvents() {
        log.info("Fetching today's events");
        LocalDate today = LocalDate.now();
        List<Event> events = eventRepository.findByDate(today);
        
        return events.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public EventDTO getEventById(Long id) {
        log.info("Fetching event by ID: {}", id);
        Event event = eventRepository.findByIdAndNotDeleted(id)
                .orElseThrow(() -> new IllegalArgumentException("Event not found with ID: " + id));
        
        return convertToDTO(event);
    }

    @Transactional
    public EventDTO createEvent(Long locationId, CreateEventDTO createEventDTO, 
                                MultipartFile image, User currentUser) throws IOException {
        log.info("Creating event for location ID: {}", locationId);
        
        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException("Event image is required");
        }
        
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new IllegalArgumentException("Location not found with ID: " + locationId));
        
        if (location.getDeleted()) {
            throw new IllegalArgumentException("Cannot create event for deleted location");
        }
        
        boolean isManager = managesRepository.findActiveManagement(
                currentUser.getId(), locationId, LocalDate.now()).stream()
                .anyMatch(m -> m.isActive());
        
        if (!isManager) {
            throw new IllegalArgumentException("User is not a manager of this location");
        }
        
        Event event = new Event();
        event.setName(createEventDTO.getName());
        event.setAddress(createEventDTO.getAddress());
        event.setType(createEventDTO.getType());
        event.setDate(createEventDTO.getDate());
        event.setPrice(createEventDTO.getPrice() != null ? createEventDTO.getPrice() : 0.0);
        event.setRecurrent(createEventDTO.getRecurrent() != null ? createEventDTO.getRecurrent() : false);
        event.setLocation(location);
        event.setDeleted(false);
        
        Event savedEvent = eventRepository.save(event);
        
        String imageUrl = fileStorageService.saveImage(image, "events");
        Image eventImage = new Image();
        eventImage.setPath(imageUrl);
        eventImage.setEvent(savedEvent);
        Image savedImage = imageRepository.save(eventImage);
        
        // Update the event's image reference (bidirectional relationship)
        savedEvent.setImage(savedImage);
        
        log.info("Successfully created event with ID: {}", savedEvent.getId());
        return convertToDTO(savedEvent);
    }

    @Transactional
    public EventDTO updateEvent(Long eventId, UpdateEventDTO updateEventDTO, User currentUser) {
        log.info("Updating event ID: {}", eventId);
        
        Event event = eventRepository.findByIdAndNotDeleted(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found with ID: " + eventId));
        
        boolean isManager = managesRepository.findActiveManagement(
                currentUser.getId(), event.getLocation().getId(), LocalDate.now()).stream()
                .anyMatch(m -> m.isActive());
        
        if (!isManager) {
            throw new IllegalArgumentException("User is not a manager of this event's location");
        }
        
        if (updateEventDTO.getName() != null) {
            event.setName(updateEventDTO.getName());
        }
        if (updateEventDTO.getAddress() != null) {
            event.setAddress(updateEventDTO.getAddress());
        }
        if (updateEventDTO.getType() != null) {
            event.setType(updateEventDTO.getType());
        }
        if (updateEventDTO.getDate() != null) {
            event.setDate(updateEventDTO.getDate());
        }
        if (updateEventDTO.getPrice() != null) {
            event.setPrice(updateEventDTO.getPrice());
        }
        if (updateEventDTO.getRecurrent() != null) {
            event.setRecurrent(updateEventDTO.getRecurrent());
        }
        
        Event updatedEvent = eventRepository.save(event);
        log.info("Successfully updated event with ID: {}", eventId);
        return convertToDTO(updatedEvent);
    }

    @Transactional
    public void deleteEvent(Long eventId, User currentUser) {
        log.info("Deleting event ID: {}", eventId);
        
        Event event = eventRepository.findByIdAndNotDeleted(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found with ID: " + eventId));
        
        boolean isManager = managesRepository.findActiveManagement(
                currentUser.getId(), event.getLocation().getId(), LocalDate.now()).stream()
                .anyMatch(m -> m.isActive());
        
        if (!isManager) {
            throw new IllegalArgumentException("User is not a manager of this event's location");
        }
        
        event.setDeleted(true);
        eventRepository.save(event);
        log.info("Successfully deleted event with ID: {}", eventId);
    }

    @Transactional
    public EventDTO updateEventImage(Long eventId, MultipartFile image, User currentUser) throws IOException {
        log.info("Updating image for event ID: {}", eventId);
        
        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException("Image file is required");
        }
        
        Event event = eventRepository.findByIdAndNotDeleted(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found with ID: " + eventId));
        
        boolean isManager = managesRepository.findActiveManagement(
                currentUser.getId(), event.getLocation().getId(), LocalDate.now()).stream()
                .anyMatch(m -> m.isActive());
        
        if (!isManager) {
            throw new IllegalArgumentException("User is not a manager of this event's location");
        }
        
        // Delete old image if exists
        if (event.getImage() != null) {
            String oldImagePath = event.getImage().getPath();
            Image oldImage = event.getImage();
            
            // Break the relationship first
            event.setImage(null);
            oldImage.setEvent(null);
            
            // Delete from database
            imageRepository.delete(oldImage);
            
            // Flush to ensure deletion is committed before inserting new image
            entityManager.flush();
            
            // Delete the file from filesystem
            fileStorageService.deleteImage(oldImagePath);
        }
        
        // Save new image
        String imageUrl = fileStorageService.saveImage(image, "events");
        Image eventImage = new Image();
        eventImage.setPath(imageUrl);
        eventImage.setEvent(event);
        Image savedImage = imageRepository.save(eventImage);
        
        // Update the event's image reference (bidirectional relationship)
        event.setImage(savedImage);
        Event updatedEvent = eventRepository.save(event);
        
        log.info("Successfully updated image for event ID: {}", eventId);
        return convertToDTO(updatedEvent);
    }

    @Transactional(readOnly = true)
    public Long countEventOccurrences(Long eventId, LocalDate untilDate) {
        log.info("Counting occurrences for event ID: {} until date: {}", eventId, untilDate);
        
        Event event = eventRepository.findByIdAndNotDeleted(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found with ID: " + eventId));
        
        if (!event.getRecurrent()) {
            throw new IllegalArgumentException("Event is not recurrent");
        }
        
        Long count = eventRepository.countOccurrencesUntilDate(eventId, untilDate);
        log.info("Event ID: {} has {} occurrences until {}", eventId, count, untilDate);
        return count;
    }

    private EventDTO convertToDTO(Event event) {
        EventDTO dto = new EventDTO();
        dto.setId(event.getId());
        dto.setName(event.getName());
        dto.setAddress(event.getAddress());
        dto.setType(event.getType());
        dto.setDate(event.getDate());
        dto.setPrice(event.getPrice());
        dto.setRecurrent(event.getRecurrent());
        
        if (event.getLocation() != null) {
            dto.setLocationId(event.getLocation().getId());
            dto.setLocationName(event.getLocation().getName());
        }
        
        if (event.getImage() != null) {
            dto.setImageUrl(event.getImage().getPath());
        }
        
        return dto;
    }
}
