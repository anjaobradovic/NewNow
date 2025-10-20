package rs.ftn.newnow.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.ftn.newnow.dto.EventDTO;
import rs.ftn.newnow.dto.LocationDTO;
import rs.ftn.newnow.model.Event;
import rs.ftn.newnow.model.Location;
import rs.ftn.newnow.repository.EventRepository;
import rs.ftn.newnow.repository.LocationRepository;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class SearchService {

    private final LocationRepository locationRepository;
    private final EventRepository eventRepository;

    public Page<LocationDTO> searchLocations(String q, String type, String address, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Location> locations;

        if (q != null && !q.trim().isEmpty()) {
            locations = locationRepository.searchByQuery(q, pageable);
        } else if ((type != null && !type.trim().isEmpty()) || (address != null && !address.trim().isEmpty())) {
            locations = locationRepository.searchByTypeAndAddress(type, address, pageable);
        } else {
            locations = locationRepository.findByDeletedFalse(pageable);
        }

        return locations.map(this::convertToDTO);
    }

    public Page<EventDTO> searchEvents(
            String type,
            Long locationId,
            String address,
            Double minPrice,
            Double maxPrice,
            LocalDate startDate,
            LocalDate endDate,
            Boolean past,
            Boolean future,
            int page,
            int size) {

        Pageable pageable = PageRequest.of(page, size);
        LocalDate referenceDate = LocalDate.now();
        LocalDate finalStartDate = startDate;
        LocalDate finalEndDate = endDate;

        if (past != null && past) {
            finalEndDate = referenceDate.minusDays(1);
        } else if (future != null && future) {
            finalStartDate = referenceDate;
        }

        Page<Event> events = eventRepository.searchEvents(
                type, locationId, address, minPrice, maxPrice, finalStartDate, finalEndDate, pageable);

        return events.map(this::convertToDTO);
    }

    private LocationDTO convertToDTO(Location location) {
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

    private EventDTO convertToDTO(Event event) {
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
}
