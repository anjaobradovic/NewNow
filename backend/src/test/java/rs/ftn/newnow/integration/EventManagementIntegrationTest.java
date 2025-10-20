package rs.ftn.newnow.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import rs.ftn.newnow.dto.AssignManagerDTO;
import rs.ftn.newnow.dto.UpdateEventDTO;
import rs.ftn.newnow.model.*;
import rs.ftn.newnow.repository.*;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class EventManagementIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private ManagesRepository managesRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User managerUser;
    private User regularUser;
    private Location testLocation;
    private Event testEvent;

    @BeforeEach
    void setUp() {
        eventRepository.deleteAll();
        managesRepository.deleteAll();
        locationRepository.deleteAll();
        userRepository.deleteAll();

        managerUser = new User();
        managerUser.setEmail("manager@test.com");
        managerUser.setPassword(passwordEncoder.encode("password"));
        managerUser.setName("Test Manager");
        managerUser.setPhoneNumber("1234567890");
        managerUser.setCreatedAt(LocalDate.now());
        managerUser = userRepository.save(managerUser);

        regularUser = new User();
        regularUser.setEmail("user@test.com");
        regularUser.setPassword(passwordEncoder.encode("password"));
        regularUser.setName("Test User");
        regularUser.setPhoneNumber("0987654321");
        regularUser.setCreatedAt(LocalDate.now());
        regularUser = userRepository.save(regularUser);

        testLocation = new Location();
        testLocation.setName("Test Location");
        testLocation.setAddress("123 Test Street");
        testLocation.setType("Club");
        testLocation.setDeleted(false);
        testLocation.setTotalRating(0.0);
        testLocation.setCreatedAt(LocalDate.now());
        testLocation = locationRepository.save(testLocation);

        Manages manages = new Manages();
        manages.setUser(managerUser);
        manages.setLocation(testLocation);
        managesRepository.save(manages);

        testEvent = new Event();
        testEvent.setName("Test Event");
        testEvent.setAddress("Event Address");
        testEvent.setType("Concert");
        testEvent.setDate(LocalDate.now().plusDays(7));
        testEvent.setPrice(100.0);
        testEvent.setRecurrent(true);
        testEvent.setDeleted(false);
        testEvent.setLocation(testLocation);
        testEvent = eventRepository.save(testEvent);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void fullManagerAndEventWorkflow() throws Exception {
        AssignManagerDTO assignDTO = new AssignManagerDTO(regularUser.getId());
        
        mockMvc.perform(post("/api/admin/locations/" + testLocation.getId() + "/managers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(assignDTO)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/admin/locations/" + testLocation.getId() + "/managers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].userId").exists());
    }

    @Test
    void searchEventsWithFilters() throws Exception {
        mockMvc.perform(get("/api/events")
                .param("type", "Concert")
                .param("isFree", "false")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void getTodayEvents() throws Exception {
        mockMvc.perform(get("/api/events/today"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getEventById() throws Exception {
        mockMvc.perform(get("/api/events/" + testEvent.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testEvent.getId()));
    }

    @Test
    @WithMockUser(username = "manager@test.com", roles = "MANAGER")
    void createAndManageEvent() throws Exception {
        MockMultipartFile image = new MockMultipartFile(
                "image", "test.jpg", "image/jpeg", "test image content".getBytes());

        mockMvc.perform(multipart("/api/locations/" + testLocation.getId() + "/events")
                .file(image)
                .param("name", "Integration Test Event")
                .param("address", "Test Address")
                .param("type", "Party")
                .param("date", LocalDate.now().plusDays(7).toString())
                .param("price", "50.0")
                .param("recurrent", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Integration Test Event"));
    }

    @Test
    @WithMockUser(username = "manager@test.com", roles = "MANAGER")
    void updateEvent() throws Exception {
        UpdateEventDTO updateDTO = new UpdateEventDTO();
        updateDTO.setName("Updated Event Name");
        updateDTO.setPrice(200.0);

        mockMvc.perform(put("/api/events/" + testEvent.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "manager@test.com", roles = "MANAGER")
    void deleteEvent() throws Exception {
        mockMvc.perform(delete("/api/events/" + testEvent.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Event deleted successfully"));
    }

    @Test
    @WithMockUser(username = "manager@test.com", roles = "MANAGER")
    void updateEventImage() throws Exception {
        MockMultipartFile newImage = new MockMultipartFile(
                "image", "new.jpg", "image/jpeg", "new image content".getBytes());

        mockMvc.perform(multipart("/api/events/" + testEvent.getId() + "/image")
                .file(newImage)
                .with(request -> {
                    request.setMethod("PUT");
                    return request;
                }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Event image updated successfully"));
    }

    @Test
    void countEventOccurrences() throws Exception {
        LocalDate untilDate = LocalDate.now().plusMonths(6);
        
        mockMvc.perform(get("/api/events/" + testEvent.getId() + "/occurrences/count")
                .param("untilDate", untilDate.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").isNumber());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void removeManager() throws Exception {
        mockMvc.perform(delete("/api/admin/locations/" + testLocation.getId() + "/managers/" + managerUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Manager removed successfully"));
    }

    @Test
    void unauthorizedAccessToManagerEndpoints() throws Exception {
        mockMvc.perform(get("/api/admin/locations/" + testLocation.getId() + "/managers"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void forbiddenAccessToCreateEvent() throws Exception {
        MockMultipartFile image = new MockMultipartFile(
                "image", "test.jpg", "image/jpeg", "test".getBytes());

        mockMvc.perform(multipart("/api/locations/" + testLocation.getId() + "/events")
                .file(image)
                .param("name", "Test")
                .param("address", "Test Address")
                .param("type", "Concert")
                .param("date", LocalDate.now().plusDays(7).toString())
                .param("price", "50.0")
                .param("recurrent", "false"))
                .andExpect(status().isForbidden());
    }
}
