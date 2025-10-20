package rs.ftn.newnow.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import rs.ftn.newnow.dto.*;
import rs.ftn.newnow.model.Event;
import rs.ftn.newnow.model.Location;
import rs.ftn.newnow.model.Review;
import rs.ftn.newnow.repository.EventRepository;
import rs.ftn.newnow.repository.LocationRepository;
import rs.ftn.newnow.repository.ManagesRepository;
import rs.ftn.newnow.repository.ReviewRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LocationServiceTest {

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private ManagesRepository managesRepository;

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private LocationService locationService;

    private Location testLocation;
    private CreateLocationDTO createDTO;
    private UpdateLocationDTO updateDTO;
    private PatchLocationDTO patchDTO;

    @BeforeEach
    void setUp() {
        testLocation = new Location();
        testLocation.setId(1L);
        testLocation.setName("Test Location");
        testLocation.setAddress("Test Address");
        testLocation.setType("Club");
        testLocation.setDescription("Test description");
        testLocation.setImageUrl("/test/image.jpg");
        testLocation.setDeleted(false);
        testLocation.setTotalRating(4.5);
        testLocation.setCreatedAt(LocalDate.now());

        createDTO = new CreateLocationDTO();
        createDTO.setName("New Location");
        createDTO.setAddress("New Address");
        createDTO.setType("Bar");
        createDTO.setDescription("New description");

        updateDTO = new UpdateLocationDTO();
        updateDTO.setName("Updated Location");
        updateDTO.setAddress("Updated Address");
        updateDTO.setType("Restaurant");
        updateDTO.setDescription("Updated description");

        patchDTO = new PatchLocationDTO();
        patchDTO.setAddress("Patched Address");
    }

    @Test
    void getLocations_WithoutSearch_ShouldReturnAllLocations() {
        List<Location> locations = List.of(testLocation);
        Page<Location> page = new PageImpl<>(locations);
        
        when(locationRepository.findByDeletedFalse(any(Pageable.class))).thenReturn(page);

        LocationPageResponse response = locationService.getLocations(null, 0, 10);

        assertNotNull(response);
        assertEquals(1, response.getLocations().size());
        assertEquals("Test Location", response.getLocations().get(0).getName());
        verify(locationRepository).findByDeletedFalse(any(Pageable.class));
    }

    @Test
    void getLocations_WithSearch_ShouldReturnFilteredLocations() {
        List<Location> locations = List.of(testLocation);
        Page<Location> page = new PageImpl<>(locations);
        
        when(locationRepository.searchLocations(anyString(), any(Pageable.class))).thenReturn(page);

        LocationPageResponse response = locationService.getLocations("Test", 0, 10);

        assertNotNull(response);
        assertEquals(1, response.getLocations().size());
        verify(locationRepository).searchLocations(eq("Test"), any(Pageable.class));
    }

    @Test
    void getLocationDetails_WithValidId_ShouldReturnDetails() {
        when(locationRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(testLocation));
        when(reviewRepository.findByLocationIdAndNotDeleted(anyLong(), any(Pageable.class))).thenReturn(Page.empty());
        when(eventRepository.findUpcomingByLocation(anyLong(), any(LocalDate.class), any(Pageable.class)))
                .thenReturn(Page.empty());

        LocationDetailsDTO details = locationService.getLocationDetails(1L);

        assertNotNull(details);
        assertEquals("Test Location", details.getName());
        assertEquals("Test Address", details.getAddress());
        assertNotNull(details.getUpcomingEvents());
        verify(locationRepository).findByIdAndDeletedFalse(1L);
    }

    @Test
    void getLocationDetails_WithInvalidId_ShouldThrowException() {
        when(locationRepository.findByIdAndDeletedFalse(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> locationService.getLocationDetails(999L));
    }

    @Test
    void createLocation_WithValidData_ShouldCreateLocation() throws Exception {
        MockMultipartFile image = new MockMultipartFile(
                "image", "test.jpg", "image/jpeg", "test".getBytes()
        );
        
        when(fileStorageService.saveImage(any(), eq("locations"))).thenReturn("/uploads/locations/test.jpg");
        when(locationRepository.save(any(Location.class))).thenReturn(testLocation);

        LocationDTO result = locationService.createLocation(createDTO, image);

        assertNotNull(result);
        assertEquals("Test Location", result.getName());
        verify(fileStorageService).saveImage(any(), eq("locations"));
        verify(locationRepository).save(any(Location.class));
    }

    @Test
    void createLocation_WithoutImage_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class, 
                () -> locationService.createLocation(createDTO, null));
    }

    @Test
    void updateLocation_WithValidData_ShouldUpdateLocation() {
        when(locationRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(testLocation));
        when(locationRepository.save(any(Location.class))).thenReturn(testLocation);

        LocationDTO result = locationService.updateLocation(1L, updateDTO);

        assertNotNull(result);
        verify(locationRepository).findByIdAndDeletedFalse(1L);
        verify(locationRepository).save(any(Location.class));
    }

    @Test
    void patchLocation_WithPartialData_ShouldUpdateOnlyProvidedFields() {
        when(locationRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(testLocation));
        when(locationRepository.save(any(Location.class))).thenReturn(testLocation);

        LocationDTO result = locationService.patchLocation(1L, patchDTO);

        assertNotNull(result);
        verify(locationRepository).findByIdAndDeletedFalse(1L);
        verify(locationRepository).save(any(Location.class));
    }

    @Test
    void deleteLocation_WithValidId_ShouldSoftDeleteLocation() {
        when(locationRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(testLocation));

        locationService.deleteLocation(1L);

        assertTrue(testLocation.getDeleted());
        verify(locationRepository).save(testLocation);
    }

    @Test
    void updateLocationImage_WithValidImage_ShouldUpdateImage() throws Exception {
        MockMultipartFile newImage = new MockMultipartFile(
                "image", "new.jpg", "image/jpeg", "new".getBytes()
        );
        
        when(locationRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(testLocation));
        when(fileStorageService.saveImage(any(), eq("locations"))).thenReturn("/uploads/locations/new.jpg");
        when(locationRepository.save(any(Location.class))).thenReturn(testLocation);

        LocationDTO result = locationService.updateLocationImage(1L, newImage);

        assertNotNull(result);
        verify(fileStorageService).saveImage(newImage, "locations");
        verify(fileStorageService).deleteImage("/test/image.jpg");
        verify(locationRepository).save(any(Location.class));
    }

    @Test
    void getUpcomingEvents_ShouldReturnPagedEvents() {
        Page<Event> eventPage = Page.empty();
        when(locationRepository.existsById(1L)).thenReturn(true);
        when(eventRepository.findUpcomingByLocation(anyLong(), any(LocalDate.class), any(Pageable.class)))
                .thenReturn(eventPage);

        Page<EventDTO> result = locationService.getUpcomingEvents(1L, LocalDate.now(), 0, 10);

        assertNotNull(result);
        verify(eventRepository).findUpcomingByLocation(anyLong(), any(LocalDate.class), any(Pageable.class));
    }

    @Test
    void getLocationReviews_ShouldReturnPagedReviews() {
        Page<Review> reviewPage = Page.empty();
        when(locationRepository.existsById(1L)).thenReturn(true);
        when(reviewRepository.findByLocationIdAndNotDeleted(anyLong(), any(Pageable.class)))
                .thenReturn(reviewPage);

        Page<ReviewDTO> result = locationService.getLocationReviews(1L, "date", "desc", 0, 10);

        assertNotNull(result);
        verify(reviewRepository).findByLocationIdAndNotDeleted(anyLong(), any(Pageable.class));
    }

    @Test
    void getPopularLocations_ShouldReturnLimitedResults() {
        List<Location> locations = List.of(testLocation);
        when(locationRepository.findPopularLocations(any(Pageable.class))).thenReturn(locations);

        List<LocationDTO> result = locationService.getPopularLocations(5);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(locationRepository).findPopularLocations(any(Pageable.class));
    }

    @Test
    void isUserManagerOfLocation_WithActiveManagement_ShouldReturnTrue() {
        when(managesRepository.findActiveManagement(anyLong(), anyLong(), any(LocalDate.class)))
                .thenReturn(List.of());

        boolean result = locationService.isUserManagerOfLocation(1L, 1L);

        assertFalse(result);
        verify(managesRepository).findActiveManagement(anyLong(), anyLong(), any(LocalDate.class));
    }
}
