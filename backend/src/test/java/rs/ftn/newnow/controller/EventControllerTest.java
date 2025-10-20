package rs.ftn.newnow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import rs.ftn.newnow.dto.EventDTO;
import rs.ftn.newnow.dto.UpdateEventDTO;
import rs.ftn.newnow.model.User;
import rs.ftn.newnow.model.enums.Role;
import rs.ftn.newnow.repository.UserRepository;
import rs.ftn.newnow.service.EventService;

import java.time.LocalDate;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EventService eventService;

    @MockBean
    private UserRepository userRepository;

    private EventDTO eventDTO;
    private User manager;

    @BeforeEach
    void setUp() {
        manager = new User();
        manager.setId(1L);
        manager.setEmail("manager@test.com");
        manager.setRoles(new HashSet<>(Arrays.asList(Role.ROLE_USER, Role.ROLE_MANAGER)));

        eventDTO = new EventDTO();
        eventDTO.setId(1L);
        eventDTO.setName("Test Event");
        eventDTO.setAddress("Test Address");
        eventDTO.setType("Concert");
        eventDTO.setDate(LocalDate.now());
        eventDTO.setPrice(100.0);
        eventDTO.setRecurrent(false);
        eventDTO.setLocationId(1L);
        eventDTO.setLocationName("Test Location");
        eventDTO.setImageUrl("/uploads/events/test.jpg");
    }

    @Test
    void searchEvents_Success() throws Exception {
        Page<EventDTO> page = new PageImpl<>(Collections.singletonList(eventDTO));
        when(eventService.searchEvents(any(), any(), any(), any(), any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(page);

        mockMvc.perform(get("/api/events")
                .param("type", "Concert")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Test Event"))
                .andExpect(jsonPath("$.content[0].type").value("Concert"));
    }

    @Test
    void getTodayEvents_Success() throws Exception {
        List<EventDTO> events = Collections.singletonList(eventDTO);
        when(eventService.getTodayEvents()).thenReturn(events);

        mockMvc.perform(get("/api/events/today"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Test Event"))
                .andExpect(jsonPath("$[0].date").value(LocalDate.now().toString()));
    }

    @Test
    void getEventById_Success() throws Exception {
        when(eventService.getEventById(anyLong())).thenReturn(eventDTO);

        mockMvc.perform(get("/api/events/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Test Event"));
    }

    @Test
    void getEventById_NotFound() throws Exception {
        when(eventService.getEventById(anyLong()))
                .thenThrow(new IllegalArgumentException("Event not found"));

        mockMvc.perform(get("/api/events/999"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Event not found"));
    }

    @Test
    @WithMockUser(username = "manager@test.com", roles = "MANAGER")
    void createEvent_Success() throws Exception {
        MockMultipartFile image = new MockMultipartFile(
                "image", "test.jpg", "image/jpeg", "test image".getBytes());

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(manager));
        when(eventService.createEvent(anyLong(), any(), any(), any())).thenReturn(eventDTO);

        mockMvc.perform(multipart("/api/locations/1/events")
                .file(image)
                .param("name", "New Event")
                .param("address", "New Address")
                .param("type", "Concert")
                .param("date", LocalDate.now().toString())
                .param("price", "100.0")
                .param("recurrent", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Event"));

        verify(eventService).createEvent(eq(1L), any(), any(), any());
    }

    @Test
    @WithMockUser(username = "manager@test.com", roles = "MANAGER")
    void createEvent_NoImage() throws Exception {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(manager));
        when(eventService.createEvent(anyLong(), any(), any(), any()))
                .thenThrow(new IllegalArgumentException("Event image is required"));

        mockMvc.perform(multipart("/api/locations/1/events")
                .param("name", "New Event")
                .param("address", "New Address")
                .param("type", "Concert")
                .param("date", LocalDate.now().toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "manager@test.com", roles = "MANAGER")
    void updateEvent_Success() throws Exception {
        UpdateEventDTO updateDTO = new UpdateEventDTO();
        updateDTO.setName("Updated Event");
        updateDTO.setPrice(150.0);

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(manager));
        when(eventService.updateEvent(anyLong(), any(), any())).thenReturn(eventDTO);

        mockMvc.perform(put("/api/events/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test Event"));

        verify(eventService).updateEvent(eq(1L), any(), any());
    }

    @Test
    @WithMockUser(username = "manager@test.com", roles = "MANAGER")
    void deleteEvent_Success() throws Exception {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(manager));
        doNothing().when(eventService).deleteEvent(anyLong(), any());

        mockMvc.perform(delete("/api/events/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Event deleted successfully"));

        verify(eventService).deleteEvent(eq(1L), any());
    }

    @Test
    @WithMockUser(username = "manager@test.com", roles = "MANAGER")
    void updateEventImage_Success() throws Exception {
        MockMultipartFile image = new MockMultipartFile(
                "image", "new.jpg", "image/jpeg", "new image".getBytes());

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(manager));
        doNothing().when(eventService).updateEventImage(anyLong(), any(), any());

        mockMvc.perform(multipart("/api/events/1/image")
                .file(image)
                .with(request -> {
                    request.setMethod("PUT");
                    return request;
                }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Event image updated successfully"));

        verify(eventService).updateEventImage(eq(1L), any(), any());
    }

    @Test
    void countEventOccurrences_Success() throws Exception {
        LocalDate untilDate = LocalDate.now().plusMonths(3);
        when(eventService.countEventOccurrences(anyLong(), any(LocalDate.class)))
                .thenReturn(12L);

        mockMvc.perform(get("/api/events/1/occurrences/count")
                .param("untilDate", untilDate.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(12));

        verify(eventService).countEventOccurrences(1L, untilDate);
    }

    @Test
    void countEventOccurrences_NotRecurrent() throws Exception {
        when(eventService.countEventOccurrences(anyLong(), any(LocalDate.class)))
                .thenThrow(new IllegalArgumentException("Event is not recurrent"));

        mockMvc.perform(get("/api/events/1/occurrences/count")
                .param("untilDate", LocalDate.now().toString()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Event is not recurrent"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void createEvent_Unauthorized() throws Exception {
        MockMultipartFile image = new MockMultipartFile(
                "image", "test.jpg", "image/jpeg", "test".getBytes());

        mockMvc.perform(multipart("/api/locations/1/events")
                .file(image)
                .param("name", "New Event")
                .param("address", "Test Address")
                .param("type", "Test Type")
                .param("date", LocalDate.now().toString()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void createEvent_Forbidden() throws Exception {
        MockMultipartFile image = new MockMultipartFile(
                "image", "test.jpg", "image/jpeg", "test".getBytes());

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(manager));
        when(eventService.createEvent(anyLong(), any(), any(), any()))
                .thenThrow(new IllegalArgumentException("User is not a manager of this location"));

        mockMvc.perform(multipart("/api/locations/1/events")
                .file(image)
                .param("name", "New Event")
                .param("address", "Address")
                .param("type", "Type")
                .param("date", LocalDate.now().toString()))
                .andExpect(status().isBadRequest());
    }
}
