package rs.ftn.newnow.integration;

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
import rs.ftn.newnow.model.Event;
import rs.ftn.newnow.model.Location;
import rs.ftn.newnow.model.User;
import rs.ftn.newnow.model.enums.Role;
import rs.ftn.newnow.repository.EventRepository;
import rs.ftn.newnow.repository.LocationRepository;
import rs.ftn.newnow.repository.UserRepository;

import java.time.LocalDate;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ImageUploadIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    private Location location;
    private Event event;
    private User user;

    @BeforeEach
    void setUp() {
        location = new Location();
        location.setName("Test Location");
        location.setAddress("Test Address");
        location.setType("restaurant");
        location.setDeleted(false);
        location = locationRepository.save(location);

        event = new Event();
        event.setName("Test Event");
        event.setAddress("Test Address");
        event.setType("concert");
        event.setDate(LocalDate.now().plusDays(7));
        event.setLocation(location);
        event.setDeleted(false);
        event = eventRepository.save(event);

        user = new User();
        user.setEmail("testuser@example.com");
        user.setPassword("password");
        user.setName("Test User");
        user.setRoles(Set.of(Role.ROLE_USER));
        user = userRepository.save(user);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateLocationImage_AsAdmin_Success() throws Exception {
        MockMultipartFile image = new MockMultipartFile(
                "image",
                "test-image.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        mockMvc.perform(multipart("/api/locations/{id}/image", location.getId())
                        .file(image)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(location.getId()))
                .andExpect(jsonPath("$.imageUrl").exists());
    }

    @Test
    @WithMockUser(username = "testuser@example.com", roles = "USER")
    void updateLocationImage_AsUser_Forbidden() throws Exception {
        MockMultipartFile image = new MockMultipartFile(
                "image",
                "test-image.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content".getBytes()
        );

        mockMvc.perform(multipart("/api/locations/{id}/image", location.getId())
                        .file(image)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "testuser@example.com", roles = "USER")
    void updateUserAvatar_Success() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "avatar.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "avatar content".getBytes()
        );

        mockMvc.perform(multipart("/api/users/me/avatar")
                        .file(file)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void updateUserAvatar_Unauthenticated_ReturnsUnauthorized() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "avatar.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "avatar content".getBytes()
        );

        mockMvc.perform(multipart("/api/users/me/avatar")
                        .file(file)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateLocationImage_WithInvalidFile_ReturnsBadRequest() throws Exception {
        MockMultipartFile invalidFile = new MockMultipartFile(
                "image",
                "test.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "not an image".getBytes()
        );

        mockMvc.perform(multipart("/api/locations/{id}/image", location.getId())
                        .file(invalidFile)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        }))
                .andExpect(status().isBadRequest());
    }
}
