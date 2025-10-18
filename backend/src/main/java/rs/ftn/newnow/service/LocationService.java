package rs.ftn.newnow.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.ftn.newnow.dto.LocationDTO;
import rs.ftn.newnow.model.Location;
import rs.ftn.newnow.repository.LocationRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LocationService {

    private final LocationRepository locationRepository;

    @Transactional(readOnly = true)
    public List<LocationDTO> getPopularLocations() {
        log.info("Fetching popular locations");
        List<Location> locations = locationRepository.findTopByOrderByTotalRatingDesc();
        
        return locations.stream()
                .limit(3)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private LocationDTO convertToDTO(Location location) {
        LocationDTO dto = new LocationDTO();
        dto.setId(location.getId());
        dto.setName(location.getName());
        dto.setDescription(location.getDescription());
        dto.setAddress(location.getAddress());
        dto.setTotalRating(location.getTotalRating());
        dto.setType(location.getType());
        
        if (!location.getImages().isEmpty()) {
            dto.setImageUrl(location.getImages().iterator().next().getPath());
        }
        
        return dto;
    }
}
