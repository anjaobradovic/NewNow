package rs.ftn.newnow.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import rs.ftn.newnow.dto.*;
import rs.ftn.newnow.model.Event;
import rs.ftn.newnow.model.Location;
import rs.ftn.newnow.repository.EventRepository;
import rs.ftn.newnow.repository.LocationRepository;
import rs.ftn.newnow.service.LocationService;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class LocationIntegrationTest {

    @Autowired
    private LocationService locationService;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private EventRepository eventRepository;

    private Location testLocation;

    @BeforeEach
    void setUp() {
        locationRepository.deleteAll();
        eventRepository.deleteAll();

        testLocation = new Location();
        testLocation.setName("Integration Test Location");
        testLocation.setAddress("Test Address 789");
        testLocation.setType("Theatre");
        testLocation.setDescription("Integration test description");
        testLocation.setImageUrl("/test/integration.jpg");
        testLocation.setDeleted(false);
        testLocation.setTotalRating(4.8);
        testLocation = locationRepository.save(testLocation);

        Event event = new Event();
        event.setName("Test Event");
        event.setAddress("Event Address");
        event.setType("Concert");
        event.setDate(LocalDate.now().plusDays(5));
        event.setPrice(50.0);
        event.setRecurrent(false);
        event.setLocation(testLocation);
        eventRepository.save(event);
    }

    @Test
    void getLocations_ShouldReturnLocations() {
        LocationPageResponse response = locationService.getLocations(null, 0, 10);

        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
        assertEquals("Integration Test Location", response.getLocations().get(0).getName());
    }

    @Test
    void searchLocations_ShouldFindByName() {
        LocationPageResponse response = locationService.getLocations("Integration", 0, 10);

        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
        assertEquals("Integration Test Location", response.getLocations().get(0).getName());
    }

    @Test
    void searchLocations_ShouldFindByType() {
        LocationPageResponse response = locationService.getLocations("Theatre", 0, 10);

        assertNotNull(response);
        assertEquals(1, response.getTotalElements());
    }

    @Test
    void getLocationDetails_ShouldIncludeUpcomingEvents() {
        LocationDetailsDTO details = locationService.getLocationDetails(testLocation.getId());

        assertNotNull(details);
        assertEquals("Integration Test Location", details.getName());
        assertNotNull(details.getUpcomingEvents());
        assertEquals(1, details.getUpcomingEvents().size());
        assertEquals("Test Event", details.getUpcomingEvents().get(0).getName());
    }

    @Test
    void createLocation_ShouldPersistToDatabase() throws Exception {
        CreateLocationDTO dto = new CreateLocationDTO();
        dto.setName("New Integration Location");
        dto.setAddress("New Address");
        dto.setType("Cinema");
        dto.setDescription("New cinema");

        MockMultipartFile image = new MockMultipartFile(
                "image", "cinema.jpg", "image/jpeg", "cinema image".getBytes()
        );

        LocationDTO created = locationService.createLocation(dto, image);

        assertNotNull(created);
        assertNotNull(created.getId());
        
        Location savedLocation = locationRepository.findById(created.getId()).orElse(null);
        assertNotNull(savedLocation);
        assertEquals("New Integration Location", savedLocation.getName());
        assertEquals("Cinema", savedLocation.getType());
        assertNotNull(savedLocation.getImageUrl());
    }

    @Test
    void updateLocation_ShouldModifyExistingLocation() {
        UpdateLocationDTO dto = new UpdateLocationDTO();
        dto.setName("Updated Integration Location");
        dto.setAddress("Updated Address");
        dto.setType("Opera");
        dto.setDescription("Updated description");

        LocationDTO updated = locationService.updateLocation(testLocation.getId(), dto);

        assertNotNull(updated);
        assertEquals("Updated Integration Location", updated.getName());
        assertEquals("Opera", updated.getType());

        Location savedLocation = locationRepository.findById(testLocation.getId()).orElse(null);
        assertNotNull(savedLocation);
        assertEquals("Updated Integration Location", savedLocation.getName());
    }

    @Test
    void patchLocation_ShouldUpdateOnlySpecifiedFields() {
        PatchLocationDTO dto = new PatchLocationDTO();
        dto.setType("Museum");

        LocationDTO patched = locationService.patchLocation(testLocation.getId(), dto);

        assertNotNull(patched);
        assertEquals("Museum", patched.getType());
        assertEquals("Integration Test Location", patched.getName());
    }

    @Test
    void deleteLocation_ShouldMarkAsDeleted() {
        locationService.deleteLocation(testLocation.getId());

        Location deletedLocation = locationRepository.findById(testLocation.getId()).orElse(null);
        // Location is permanently deleted, not just marked as deleted
        assertNull(deletedLocation);

        assertThrows(RuntimeException.class, 
                () -> locationService.getLocationDetails(testLocation.getId()));
    }

    @Test
    void getPopularLocations_ShouldReturnLocations() {
        List<LocationDTO> popular = locationService.getPopularLocations(10);

        assertNotNull(popular);
    }
}
