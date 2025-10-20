package rs.ftn.newnow.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.ftn.newnow.dto.EventBasicDTO;
import rs.ftn.newnow.dto.LocationDTO;
import rs.ftn.newnow.dto.ReviewDetailsDTO;
import rs.ftn.newnow.model.Event;
import rs.ftn.newnow.model.Location;
import rs.ftn.newnow.repository.EventRepository;
import rs.ftn.newnow.repository.LocationRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FeedService {

    private final EventRepository eventRepository;
    private final LocationRepository locationRepository;
    private final ReviewService reviewService;

    @Transactional(readOnly = true)
    public List<EventBasicDTO> getTodayEvents() {
        List<Event> events = eventRepository.findNonRecurrentByDate(LocalDate.now());
        return events.stream()
                .map(this::mapEventToBasicDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LocationDTO> getPopularLocations(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<Location> locations = locationRepository.findPopularLocations(pageable);
        return locations.stream()
                .map(this::mapLocationToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ReviewDetailsDTO> getPopularLocationLatestReviews() {
        Pageable pageable = PageRequest.of(0, 1);
        List<Location> popularLocations = locationRepository.findPopularLocations(pageable);
        
        if (popularLocations.isEmpty()) {
            throw new RuntimeException("No popular locations found");
        }
        
        Location mostPopular = popularLocations.get(0);
        return reviewService.getLatestReviewsForLocation(mostPopular.getId(), 3);
    }

    private EventBasicDTO mapEventToBasicDTO(Event event) {
        EventBasicDTO dto = new EventBasicDTO();
        dto.setId(event.getId());
        dto.setName(event.getName());
        dto.setType(event.getType());
        dto.setDate(event.getDate());
        dto.setRecurrent(event.getRecurrent());
        return dto;
    }

    private LocationDTO mapLocationToDTO(Location location) {
        LocationDTO dto = new LocationDTO();
        dto.setId(location.getId());
        dto.setName(location.getName());
        dto.setDescription(location.getDescription());
        dto.setAddress(location.getAddress());
        dto.setType(location.getType());
        dto.setTotalRating(location.getTotalRating());
        dto.setImageUrl(location.getImageUrl());
        return dto;
    }
}
