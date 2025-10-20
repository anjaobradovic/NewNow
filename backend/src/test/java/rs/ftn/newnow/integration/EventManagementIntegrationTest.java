package rs.ftn.newnow.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import rs.ftn.newnow.dto.AssignManagerDTO;
import rs.ftn.newnow.dto.UpdateEventDTO;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class EventManagementIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "ADMIN")
    void fullManagerAndEventWorkflow() throws Exception {
        AssignManagerDTO assignDTO = new AssignManagerDTO(2L);
        
        mockMvc.perform(post("/api/admin/locations/1/managers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(assignDTO)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/admin/locations/1/managers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(2));
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
        mockMvc.perform(get("/api/events/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser(username = "manager@test.com", roles = "MANAGER")
    void createAndManageEvent() throws Exception {
        MockMultipartFile image = new MockMultipartFile(
                "image", "test.jpg", "image/jpeg", "test image content".getBytes());

        mockMvc.perform(multipart("/api/locations/1/events")
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

        mockMvc.perform(put("/api/events/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "manager@test.com", roles = "MANAGER")
    void deleteEvent() throws Exception {
        mockMvc.perform(delete("/api/events/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Event deleted successfully"));
    }

    @Test
    @WithMockUser(username = "manager@test.com", roles = "MANAGER")
    void updateEventImage() throws Exception {
        MockMultipartFile newImage = new MockMultipartFile(
                "image", "new.jpg", "image/jpeg", "new image content".getBytes());

        mockMvc.perform(multipart("/api/events/1/image")
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
        
        mockMvc.perform(get("/api/events/1/occurrences/count")
                .param("untilDate", untilDate.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").isNumber());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void removeManager() throws Exception {
        mockMvc.perform(delete("/api/admin/locations/1/managers/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Manager removed successfully"));
    }

    @Test
    void unauthorizedAccessToManagerEndpoints() throws Exception {
        mockMvc.perform(get("/api/admin/locations/1/managers"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void forbiddenAccessToCreateEvent() throws Exception {
        MockMultipartFile image = new MockMultipartFile(
                "image", "test.jpg", "image/jpeg", "test".getBytes());

        mockMvc.perform(multipart("/api/locations/1/events")
                .file(image)
                .param("name", "Test"))
                .andExpect(status().isForbidden());
    }
}
