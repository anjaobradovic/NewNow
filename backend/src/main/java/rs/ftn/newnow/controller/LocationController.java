package rs.ftn.newnow.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.ftn.newnow.dto.LocationDTO;
import rs.ftn.newnow.service.LocationService;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
@Slf4j
public class LocationController {

    private final LocationService locationService;

    @GetMapping("/popular")
    public ResponseEntity<List<LocationDTO>> getPopularLocations() {
        try {
            List<LocationDTO> locations = locationService.getPopularLocations();
            return ResponseEntity.ok(locations);
        } catch (Exception e) {
            log.error("Error fetching popular locations", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
