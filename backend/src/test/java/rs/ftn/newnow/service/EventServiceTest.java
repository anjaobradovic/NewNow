package rs.ftn.newnow.service;

import jakarta.persistence.EntityManager;
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
import org.springframework.web.multipart.MultipartFile;
import rs.ftn.newnow.dto.CreateEventDTO;
import rs.ftn.newnow.dto.EventDTO;
import rs.ftn.newnow.dto.UpdateEventDTO;
import rs.ftn.newnow.model.*;
import rs.ftn.newnow.model.enums.Role;
import rs.ftn.newnow.repository.EventRepository;
import rs.ftn.newnow.repository.ImageRepository;
import rs.ftn.newnow.repository.LocationRepository;
import rs.ftn.newnow.repository.ManagesRepository;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private ManagesRepository managesRepository;

    @Mock
    private ImageRepository imageRepository;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private EventService eventService;

    private User user;
    private Location location;
    private Event event;
    private Image image;
    private Manages manages;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("manager@test.com");
        user.setRoles(new HashSet<>(Arrays.asList(Role.ROLE_USER, Role.ROLE_MANAGER)));

        location = new Location();
        location.setId(1L);
        location.setName("Test Location");
        location.setDeleted(false);

        image = new Image();
        image.setId(1L);
        image.setPath("/uploads/events/test.jpg");

        event = new Event();
        event.setId(1L);
        event.setName("Test Event");
        event.setAddress("Test Address");
        event.setType("Concert");
        event.setDate(LocalDate.now());
        event.setPrice(100.0);
        event.setRecurrent(false);
        event.setDeleted(false);
        event.setLocation(location);
        event.setImage(image);

        manages = new Manages();
        manages.setId(1L);
        manages.setUser(user);
        manages.setLocation(location);
        manages.setStartDate(LocalDate.now().minusDays(30));
    }

    @Test
    void searchEvents_Success() {
        Page<Event> eventPage = new PageImpl<>(Collections.singletonList(event));
        when(eventRepository.findByFilters(any(), any(), any(), any(), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(eventPage);

        Page<EventDTO> result = eventService.searchEvents(
                "Concert", 1L, "Test", 0.0, 200.0, false, false, LocalDate.now(), 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Test Event", result.getContent().get(0).getName());
    }

    @Test
    void getTodayEvents_Success() {
        when(eventRepository.findByDate(any(LocalDate.class)))
                .thenReturn(Collections.singletonList(event));

        List<EventDTO> result = eventService.getTodayEvents();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Event", result.get(0).getName());
    }

    @Test
    void getEventById_Success() {
        when(eventRepository.findByIdAndNotDeleted(anyLong())).thenReturn(Optional.of(event));

        EventDTO result = eventService.getEventById(1L);

        assertNotNull(result);
        assertEquals("Test Event", result.getName());
        assertEquals(1L, result.getLocationId());
    }

    @Test
    void getEventById_NotFound() {
        when(eventRepository.findByIdAndNotDeleted(anyLong())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> 
            eventService.getEventById(1L)
        );
    }

    @Test
    void createEvent_Success() throws Exception {
        CreateEventDTO dto = new CreateEventDTO();
        dto.setName("New Event");
        dto.setAddress("New Address");
        dto.setType("Party");
        dto.setDate(LocalDate.now().plusDays(7));
        dto.setPrice(50.0);
        dto.setRecurrent(false);

        MockMultipartFile imageFile = new MockMultipartFile(
                "image", "test.jpg", "image/jpeg", "test image content".getBytes());

        when(locationRepository.findById(anyLong())).thenReturn(Optional.of(location));
        when(managesRepository.findActiveManagement(anyLong(), anyLong(), any(LocalDate.class)))
                .thenReturn(Collections.singletonList(manages));
        when(eventRepository.save(any(Event.class))).thenReturn(event);
        when(fileStorageService.saveImage(any(MultipartFile.class), anyString()))
                .thenReturn("/uploads/events/test.jpg");
        when(imageRepository.save(any(Image.class))).thenReturn(image);

        EventDTO result = eventService.createEvent(1L, dto, imageFile, user);

        assertNotNull(result);
        verify(eventRepository).save(any(Event.class));
        verify(fileStorageService).saveImage(any(MultipartFile.class), eq("events"));
        verify(imageRepository).save(any(Image.class));
    }

    @Test
    void createEvent_NoImage() {
        CreateEventDTO dto = new CreateEventDTO();
        dto.setName("New Event");

        assertThrows(IllegalArgumentException.class, () -> 
            eventService.createEvent(1L, dto, null, user)
        );
    }

    @Test
    void createEvent_NotManager() {
        CreateEventDTO dto = new CreateEventDTO();
        dto.setName("New Event");
        
        MockMultipartFile imageFile = new MockMultipartFile(
                "image", "test.jpg", "image/jpeg", "test".getBytes());

        when(locationRepository.findById(anyLong())).thenReturn(Optional.of(location));
        when(managesRepository.findActiveManagement(anyLong(), anyLong(), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        assertThrows(IllegalArgumentException.class, () -> 
            eventService.createEvent(1L, dto, imageFile, user)
        );
    }

    @Test
    void updateEvent_Success() {
        UpdateEventDTO dto = new UpdateEventDTO();
        dto.setName("Updated Event");
        dto.setPrice(150.0);

        when(eventRepository.findByIdAndNotDeleted(anyLong())).thenReturn(Optional.of(event));
        when(managesRepository.findActiveManagement(anyLong(), anyLong(), any(LocalDate.class)))
                .thenReturn(Collections.singletonList(manages));
        when(eventRepository.save(any(Event.class))).thenReturn(event);

        EventDTO result = eventService.updateEvent(1L, dto, user);

        assertNotNull(result);
        verify(eventRepository).save(any(Event.class));
    }

    @Test
    void updateEvent_NotManager() {
        UpdateEventDTO dto = new UpdateEventDTO();
        dto.setName("Updated Event");

        when(eventRepository.findByIdAndNotDeleted(anyLong())).thenReturn(Optional.of(event));
        when(managesRepository.findActiveManagement(anyLong(), anyLong(), any(LocalDate.class)))
                .thenReturn(Collections.emptyList());

        assertThrows(IllegalArgumentException.class, () -> 
            eventService.updateEvent(1L, dto, user)
        );
    }

    @Test
    void deleteEvent_Success() {
        when(eventRepository.findByIdAndNotDeleted(anyLong())).thenReturn(Optional.of(event));
        when(managesRepository.findActiveManagement(anyLong(), anyLong(), any(LocalDate.class)))
                .thenReturn(Collections.singletonList(manages));
        when(eventRepository.save(any(Event.class))).thenReturn(event);

        eventService.deleteEvent(1L, user);

        verify(eventRepository).save(argThat(e -> e.getDeleted()));
    }

    @Test
    void updateEventImage_Success() throws Exception {
        MockMultipartFile imageFile = new MockMultipartFile(
                "image", "new.jpg", "image/jpeg", "new image".getBytes());

        when(eventRepository.findByIdAndNotDeleted(anyLong())).thenReturn(Optional.of(event));
        when(managesRepository.findActiveManagement(anyLong(), anyLong(), any(LocalDate.class)))
                .thenReturn(Collections.singletonList(manages));
        when(fileStorageService.saveImage(any(MultipartFile.class), anyString()))
                .thenReturn("/uploads/events/new.jpg");
        when(imageRepository.save(any(Image.class))).thenReturn(image);

        eventService.updateEventImage(1L, imageFile, user);

        verify(fileStorageService).deleteImage(anyString());
        verify(fileStorageService).saveImage(any(MultipartFile.class), eq("events"));
        verify(imageRepository).delete(any(Image.class));
        verify(imageRepository).save(any(Image.class));
    }

    @Test
    void countEventOccurrences_Success() {
        event.setRecurrent(true);
        LocalDate untilDate = LocalDate.now().plusMonths(3);

        when(eventRepository.findByIdAndNotDeleted(anyLong())).thenReturn(Optional.of(event));
        when(eventRepository.countOccurrencesUntilDate(anyLong(), any(LocalDate.class)))
                .thenReturn(12L);

        Long result = eventService.countEventOccurrences(1L, untilDate);

        assertEquals(12L, result);
        verify(eventRepository).countOccurrencesUntilDate(1L, untilDate);
    }

    @Test
    void countEventOccurrences_NotRecurrent() {
        event.setRecurrent(false);

        when(eventRepository.findByIdAndNotDeleted(anyLong())).thenReturn(Optional.of(event));

        assertThrows(IllegalArgumentException.class, () -> 
            eventService.countEventOccurrences(1L, LocalDate.now())
        );
    }
}
