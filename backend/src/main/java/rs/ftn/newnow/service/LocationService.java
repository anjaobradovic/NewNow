package rs.ftn.newnow.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import rs.ftn.newnow.dto.*;
import rs.ftn.newnow.model.Event;
import rs.ftn.newnow.model.Location;
import rs.ftn.newnow.model.Review;
import rs.ftn.newnow.repository.EventRepository;
import rs.ftn.newnow.repository.LocationRepository;
import rs.ftn.newnow.repository.ManagesRepository;
import rs.ftn.newnow.repository.ReviewRepository;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LocationService {

    private final LocationRepository locationRepository;
    private final ReviewRepository reviewRepository;
    private final EventRepository eventRepository;
    private final ManagesRepository managesRepository;
    private final FileStorageService fileStorageService;

    @Transactional(readOnly = true)
    public LocationPageResponse getLocations(String search, int page, int size) {
        log.info("Fetching locations - page: {}, size: {}, search: {}", page, size, search);
        Pageable pageable = PageRequest.of(page, size);
        
        Page<Location> locationPage;
        if (search != null && !search.trim().isEmpty()) {
            locationPage = locationRepository.searchLocations(search.trim(), pageable);
        } else {
            locationPage = locationRepository.findByDeletedFalse(pageable);
        }
        
        List<LocationDTO> locationDTOs = locationPage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        return new LocationPageResponse(
                locationDTOs,
                locationPage.getNumber(),
                locationPage.getTotalPages(),
                locationPage.getTotalElements()
        );
    }

    @Transactional(readOnly = true)
    public LocationDetailsDTO getLocationDetails(Long id) {
        log.info("Fetching location details for id: {}", id);
        Location location = locationRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Location not found"));
        
        return convertToDetailsDTO(location);
    }

    @Transactional
    public LocationDTO createLocation(CreateLocationDTO dto, MultipartFile image) throws IOException {
        log.info("Creating location: {}", dto.getName());
        
        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException("Image is required");
        }
        
        String imageUrl = fileStorageService.saveImage(image, "locations");
        
        Location location = new Location();
        location.setName(dto.getName());
        location.setAddress(dto.getAddress());
        location.setType(dto.getType());
        location.setDescription(dto.getDescription());
        location.setImageUrl(imageUrl);
        location.setDeleted(false);
        location.setTotalRating(0.0);
        
        location = locationRepository.save(location);
        log.info("Location created with id: {}", location.getId());
        
        return convertToDTO(location);
    }

    @Transactional
    public LocationDTO updateLocation(Long id, UpdateLocationDTO dto) {
        log.info("Updating location with id: {}", id);
        Location location = locationRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Location not found"));
        
        location.setName(dto.getName());
        location.setAddress(dto.getAddress());
        location.setType(dto.getType());
        location.setDescription(dto.getDescription());
        
        location = locationRepository.save(location);
        log.info("Location updated: {}", id);
        
        return convertToDTO(location);
    }

    @Transactional
    public LocationDTO patchLocation(Long id, PatchLocationDTO dto) {
        log.info("Patching location with id: {}", id);
        Location location = locationRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Location not found"));
        
        if (dto.getAddress() != null) {
            location.setAddress(dto.getAddress());
        }
        if (dto.getType() != null) {
            location.setType(dto.getType());
        }
        if (dto.getDescription() != null) {
            location.setDescription(dto.getDescription());
        }
        
        location = locationRepository.save(location);
        log.info("Location patched: {}", id);
        
        return convertToDTO(location);
    }

    @Transactional
    public void deleteLocation(Long id) {
        log.info("Permanently deleting location with id: {}", id);
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Location not found"));
        
        if (location.getImageUrl() != null) {
            fileStorageService.deleteImage(location.getImageUrl());
        }
        
        location.getEvents().forEach(event -> {
            if (event.getImage() != null && event.getImage().getPath() != null) {
                fileStorageService.deleteImage(event.getImage().getPath());
            }
        });
        
        locationRepository.delete(location);
        log.info("Location permanently deleted with all related data: {}", id);
    }

    @Transactional
    public LocationDTO updateLocationImage(Long id, MultipartFile image) throws IOException {
        log.info("Updating image for location: {}", id);
        Location location = locationRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Location not found"));
        
        String oldImageUrl = location.getImageUrl();
        String newImageUrl = fileStorageService.saveImage(image, "locations");
        
        location.setImageUrl(newImageUrl);
        location = locationRepository.save(location);
        
        if (oldImageUrl != null) {
            fileStorageService.deleteImage(oldImageUrl);
        }
        
        log.info("Location image updated: {}", id);
        return convertToDTO(location);
    }

    @Transactional(readOnly = true)
    public Page<EventDTO> getUpcomingEvents(Long locationId, LocalDate dateFrom, int page, int size) {
        log.info("Fetching upcoming events for location: {}", locationId);
        
        if (!locationRepository.existsById(locationId)) {
            throw new RuntimeException("Location not found");
        }
        
        LocalDate fromDate = dateFrom != null ? dateFrom : LocalDate.now();
        Pageable pageable = PageRequest.of(page, size);
        
        Page<Event> events = eventRepository.findUpcomingByLocation(locationId, fromDate, pageable);
        
        return events.map(this::convertEventToDTO);
    }

    @Transactional(readOnly = true)
    public Page<ReviewDTO> getLocationReviews(Long locationId, String sort, String order, int page, int size) {
        log.info("Fetching reviews for location: {} with sort: {}, order: {}", locationId, sort, order);
        
        if (!locationRepository.existsById(locationId)) {
            throw new RuntimeException("Location not found");
        }
        
        Pageable pageable = createPageableForReviews(sort, order, page, size);
        Page<Review> reviews = reviewRepository.findByLocationIdAndNotDeleted(locationId, pageable);
        
        return reviews.map(this::convertReviewToDTO);
    }

    @Transactional(readOnly = true)
    public List<LocationDTO> getPopularLocations(Integer limit) {
        log.info("Fetching popular locations with limit: {}", limit);
        int resultLimit = limit != null && limit > 0 ? limit : 10;
        
        Pageable pageable = PageRequest.of(0, resultLimit);
        List<Location> locations = locationRepository.findPopularLocations(pageable);
        
        return locations.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public boolean isUserManagerOfLocation(Long userId, Long locationId) {
        return !managesRepository.findActiveManagement(userId, locationId, LocalDate.now()).isEmpty();
    }

    public boolean isManagerOfLocation(Long locationId, String email) {
        locationRepository.findByIdAndDeletedFalse(locationId)
                .orElseThrow(() -> new RuntimeException("Location not found"));

        return managesRepository.findActiveManagersByLocation(locationId, LocalDate.now())
                .stream()
                .anyMatch(manages -> manages.getUser().getEmail().equals(email));
    }

    private Pageable createPageableForReviews(String sort, String order, int page, int size) {
        String sortField = "createdAt";
        if ("rating".equalsIgnoreCase(sort)) {
            sortField = "rate.overall";
        }
        
        org.springframework.data.domain.Sort.Direction direction = 
                "asc".equalsIgnoreCase(order) 
                        ? org.springframework.data.domain.Sort.Direction.ASC 
                        : org.springframework.data.domain.Sort.Direction.DESC;
        
        return PageRequest.of(page, size, org.springframework.data.domain.Sort.by(direction, sortField));
    }

    private LocationDTO convertToDTO(Location location) {
        LocationDTO dto = new LocationDTO();
        dto.setId(location.getId());
        dto.setName(location.getName());
        dto.setDescription(location.getDescription());
        dto.setAddress(location.getAddress());
        dto.setTotalRating(location.getTotalRating());
        dto.setType(location.getType());
        dto.setImageUrl(location.getImageUrl());
        return dto;
    }

    private LocationDetailsDTO convertToDetailsDTO(Location location) {
        LocationDetailsDTO dto = new LocationDetailsDTO();
        dto.setId(location.getId());
        dto.setName(location.getName());
        dto.setDescription(location.getDescription());
        dto.setAddress(location.getAddress());
        dto.setType(location.getType());
        dto.setCreatedAt(location.getCreatedAt());
        dto.setImageUrl(location.getImageUrl());
        
        List<Review> reviews = reviewRepository.findByLocationIdAndNotDeleted(location.getId(), Pageable.unpaged()).getContent();
        dto.setTotalReviews(reviews.size());
        
        Double avgRating = reviews.stream()
                .filter(r -> r.getRate() != null)
                .mapToDouble(r -> r.getRate().getAverageRating())
                .average()
                .orElse(0.0);
        dto.setAverageRating(avgRating);
        
        List<Event> upcomingEvents = eventRepository.findUpcomingByLocation(
                location.getId(), 
                LocalDate.now(), 
                PageRequest.of(0, 5)
        ).getContent();
        
        dto.setUpcomingEvents(upcomingEvents.stream()
                .map(this::convertEventToDTO)
                .collect(Collectors.toList()));
        
        return dto;
    }

    private EventDTO convertEventToDTO(Event event) {
        EventDTO dto = new EventDTO();
        dto.setId(event.getId());
        dto.setName(event.getName());
        dto.setAddress(event.getAddress());
        dto.setType(event.getType());
        dto.setDate(event.getDate());
        dto.setPrice(event.getPrice());
        dto.setRecurrent(event.getRecurrent());
        dto.setLocationId(event.getLocation().getId());
        dto.setLocationName(event.getLocation().getName());
        
        if (event.getImage() != null) {
            dto.setImageUrl(event.getImage().getPath());
        }
        
        return dto;
    }

    private ReviewDTO convertReviewToDTO(Review review) {
        ReviewDTO dto = new ReviewDTO();
        dto.setId(review.getId());
        dto.setCreatedAt(review.getCreatedAt());
        dto.setEventCount(review.getEventCount());
        dto.setHidden(review.getHidden());
        dto.setLocationId(review.getLocation().getId());
        dto.setLocationName(review.getLocation().getName());
        dto.setEventId(review.getEvent().getId());
        dto.setEventName(review.getEvent().getName());
        
        if (review.getRate() != null) {
            RateDTO rateDTO = new RateDTO();
            rateDTO.setPerformance(review.getRate().getPerformance());
            rateDTO.setSoundAndLighting(review.getRate().getSoundAndLighting());
            rateDTO.setVenue(review.getRate().getVenue());
            rateDTO.setOverallImpression(review.getRate().getOverallImpression());
            rateDTO.setAverageRating(review.getRate().getAverageRating());
            dto.setRate(rateDTO);
        }
        
        return dto;
    }
}
