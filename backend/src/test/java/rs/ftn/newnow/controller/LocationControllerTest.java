package rs.ftn.newnow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import rs.ftn.newnow.dto.PatchLocationDTO;
import rs.ftn.newnow.dto.UpdateLocationDTO;
import rs.ftn.newnow.model.Location;
import rs.ftn.newnow.repository.LocationRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class LocationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Location testLocation;

    @BeforeEach
    void setUp() {
        locationRepository.deleteAll();
        
        testLocation = new Location();
        testLocation.setName("Test Location");
        testLocation.setAddress("Test Address 123");
        testLocation.setType("Club");
        testLocation.setDescription("Test description");
        testLocation.setImageUrl("/test/image.jpg");
        testLocation.setDeleted(false);
        testLocation.setTotalRating(4.5);
        testLocation = locationRepository.save(testLocation);
    }

    @Test
    void getLocations_ShouldReturnPagedLocations() throws Exception {
        mockMvc.perform(get("/api/locations")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.locations").isArray())
                .andExpect(jsonPath("$.currentPage").value(0))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void getLocations_WithSearch_ShouldReturnFilteredLocations() throws Exception {
        mockMvc.perform(get("/api/locations")
                        .param("search", "Test")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.locations").isArray())
                .andExpect(jsonPath("$.locations[0].name").value("Test Location"));
    }

    @Test
    void getLocationDetails_ShouldReturnLocationDetails() throws Exception {
        mockMvc.perform(get("/api/locations/{id}", testLocation.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testLocation.getId()))
                .andExpect(jsonPath("$.name").value("Test Location"))
                .andExpect(jsonPath("$.address").value("Test Address 123"))
                .andExpect(jsonPath("$.type").value("Club"))
                .andExpect(jsonPath("$.averageRating").exists())
                .andExpect(jsonPath("$.upcomingEvents").isArray());
    }

    @Test
    void getLocationDetails_WithInvalidId_ShouldReturn404() throws Exception {
        mockMvc.perform(get("/api/locations/{id}", 9999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createLocation_WithValidData_ShouldCreateLocation() throws Exception {
        MockMultipartFile image = new MockMultipartFile(
                "image",
                "test.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );

        mockMvc.perform(multipart("/api/locations")
                        .file(image)
                        .param("name", "New Location")
                        .param("address", "New Address 456")
                        .param("type", "Bar")
                        .param("description", "New description"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("New Location"))
                .andExpect(jsonPath("$.address").value("New Address 456"))
                .andExpect(jsonPath("$.type").value("Bar"))
                .andExpect(jsonPath("$.imageUrl").exists());
    }

    @Test
    @WithMockUser(roles = "USER")
    void createLocation_AsUser_ShouldReturn403() throws Exception {
        MockMultipartFile image = new MockMultipartFile(
                "image",
                "test.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );

        mockMvc.perform(multipart("/api/locations")
                        .file(image)
                        .param("name", "New Location")
                        .param("address", "New Address")
                        .param("type", "Bar"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateLocation_WithValidData_ShouldUpdateLocation() throws Exception {
        UpdateLocationDTO dto = new UpdateLocationDTO();
        dto.setName("Updated Location");
        dto.setAddress("Updated Address");
        dto.setType("Restaurant");
        dto.setDescription("Updated description");

        mockMvc.perform(put("/api/locations/{id}", testLocation.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Location"))
                .andExpect(jsonPath("$.address").value("Updated Address"))
                .andExpect(jsonPath("$.type").value("Restaurant"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void patchLocation_WithPartialData_ShouldUpdateOnlyProvidedFields() throws Exception {
        PatchLocationDTO dto = new PatchLocationDTO();
        dto.setAddress("Patched Address");

        mockMvc.perform(patch("/api/locations/{id}", testLocation.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.address").value("Patched Address"))
                .andExpect(jsonPath("$.name").value("Test Location"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteLocation_ShouldSoftDeleteLocation() throws Exception {
        mockMvc.perform(delete("/api/locations/{id}", testLocation.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/locations/{id}", testLocation.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateLocationImage_WithValidImage_ShouldUpdateImage() throws Exception {
        MockMultipartFile newImage = new MockMultipartFile(
                "image",
                "new-image.jpg",
                "image/jpeg",
                "new image content".getBytes()
        );

        mockMvc.perform(multipart("/api/locations/{id}/image", testLocation.getId())
                        .file(newImage)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.imageUrl").exists());
    }

    @Test
    void getUpcomingEvents_ShouldReturnPagedEvents() throws Exception {
        mockMvc.perform(get("/api/locations/{id}/events/upcoming", testLocation.getId())
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void getLocationReviews_ShouldReturnPagedReviews() throws Exception {
        mockMvc.perform(get("/api/locations/{id}/reviews", testLocation.getId())
                        .param("sort", "rating")
                        .param("order", "desc")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void getPopularLocations_ShouldReturnPopularLocations() throws Exception {
        mockMvc.perform(get("/api/locations/popular")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getPopularLocations_WithDefaultLimit_ShouldReturnLocations() throws Exception {
        mockMvc.perform(get("/api/locations/popular"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
