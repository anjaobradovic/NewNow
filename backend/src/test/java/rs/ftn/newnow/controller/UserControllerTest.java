package rs.ftn.newnow.controller;

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
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import rs.ftn.newnow.dto.ChangePasswordDTO;
import rs.ftn.newnow.dto.UpdateProfileDTO;
import rs.ftn.newnow.model.*;
import rs.ftn.newnow.model.enums.Role;
import rs.ftn.newnow.repository.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class UserControllerTest {

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
    private ReviewRepository reviewRepository;

    @Autowired
    private ManagesRepository managesRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private Location testLocation;
    private Event testEvent;

    @BeforeEach
    @Transactional
    void setUp() {
        testUser = new User();
        testUser.setEmail("testuser@example.com");
        testUser.setPassword(passwordEncoder.encode("password123"));
        testUser.setName("Test User");
        testUser.setPhoneNumber("123456789");
        testUser.setBirthday(LocalDate.of(1990, 1, 1));
        testUser.setAddress("Test Address");
        testUser.setCity("Test City");
        testUser.setCreatedAt(LocalDate.now());
        Set<Role> roles = new HashSet<>();
        roles.add(Role.ROLE_USER);
        testUser.setRoles(roles);
        testUser = userRepository.save(testUser);

        testLocation = new Location();
        testLocation.setName("Test Location");
        testLocation.setDescription("Test Description");
        testLocation.setAddress("Location Address");
        testLocation.setType("Restaurant");
        testLocation.setCreatedAt(LocalDate.now());
        testLocation = locationRepository.save(testLocation);

        testEvent = new Event();
        testEvent.setName("Test Event");
        testEvent.setAddress("Event Address");
        testEvent.setType("Concert");
        testEvent.setDate(LocalDate.now().plusDays(7));
        testEvent.setPrice(50.0);
        testEvent.setRecurrent(false);
        testEvent.setLocation(testLocation);
        testEvent = eventRepository.save(testEvent);
    }

    @Test
    @WithMockUser(username = "testuser@example.com")
    void getMyProfile_Success() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("testuser@example.com"))
                .andExpect(jsonPath("$.name").value("Test User"))
                .andExpect(jsonPath("$.city").value("Test City"));
    }

    @Test
    void getMyProfile_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "testuser@example.com")
    void updateMyProfile_Success() throws Exception {
        UpdateProfileDTO updateDTO = new UpdateProfileDTO();
        updateDTO.setName("Updated Name");
        updateDTO.setCity("Updated City");
        updateDTO.setPhoneNumber("987654321");

        mockMvc.perform(patch("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.city").value("Updated City"))
                .andExpect(jsonPath("$.phoneNumber").value("987654321"));
    }

    @Test
    @WithMockUser(username = "testuser@example.com")
    void updateMyAvatar_Success() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "avatar.jpg",
                "image/jpeg",
                "test image content".getBytes()
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
    @WithMockUser(username = "testuser@example.com")
    @Transactional
    void getMyReviews_Success() throws Exception {
        Review review = new Review();
        review.setUser(testUser);
        review.setLocation(testLocation);
        review.setEvent(testEvent);
        review.setCreatedAt(LocalDateTime.now());
        review.setEventCount(1);
        review.setHidden(false);
        review.setDeleted(false);

        Rate rate = new Rate();
        rate.setPerformance(5);
        rate.setSoundLight(4);
        rate.setSpace(5);
        rate.setOverall(5);
        rate.setReview(review);
        review.setRate(rate);

        reviewRepository.save(review);

        mockMvc.perform(get("/api/users/me/reviews")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "date")
                        .param("order", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].locationName").value("Test Location"));
    }

    @Test
    @WithMockUser(username = "testuser@example.com")
    @Transactional
    void getMyManagedLocations_Success() throws Exception {
        Manages manages = new Manages();
        manages.setUser(testUser);
        manages.setLocation(testLocation);
        manages.setStartDate(LocalDate.now().minusMonths(1));
        manages.setEndDate(null);
        managesRepository.save(manages);

        mockMvc.perform(get("/api/users/me/managed-locations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].locationName").value("Test Location"))
                .andExpect(jsonPath("$[0].isActive").value(true));
    }

    @Test
    @WithMockUser(username = "testuser@example.com")
    void changePassword_Success() throws Exception {
        ChangePasswordDTO changePasswordDTO = new ChangePasswordDTO();
        changePasswordDTO.setCurrentPassword("password123");
        changePasswordDTO.setNewPassword("newPassword123");
        changePasswordDTO.setConfirmPassword("newPassword123");

        mockMvc.perform(post("/api/users/me/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changePasswordDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password changed successfully"));
    }

    @Test
    @WithMockUser(username = "testuser@example.com")
    void changePassword_WrongCurrentPassword() throws Exception {
        ChangePasswordDTO changePasswordDTO = new ChangePasswordDTO();
        changePasswordDTO.setCurrentPassword("wrongPassword");
        changePasswordDTO.setNewPassword("newPassword123");
        changePasswordDTO.setConfirmPassword("newPassword123");

        mockMvc.perform(post("/api/users/me/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changePasswordDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Current password is incorrect"));
    }

    @Test
    @WithMockUser(username = "testuser@example.com")
    void changePassword_PasswordMismatch() throws Exception {
        ChangePasswordDTO changePasswordDTO = new ChangePasswordDTO();
        changePasswordDTO.setCurrentPassword("password123");
        changePasswordDTO.setNewPassword("newPassword123");
        changePasswordDTO.setConfirmPassword("differentPassword");

        mockMvc.perform(post("/api/users/me/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changePasswordDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("New password and confirmation do not match"));
    }

    @Test
    @WithMockUser(username = "testuser@example.com")
    void changePassword_InvalidFormat() throws Exception {
        ChangePasswordDTO changePasswordDTO = new ChangePasswordDTO();
        changePasswordDTO.setCurrentPassword("password123");
        changePasswordDTO.setNewPassword("123");
        changePasswordDTO.setConfirmPassword("123");

        mockMvc.perform(post("/api/users/me/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changePasswordDTO)))
                .andExpect(status().isBadRequest());
    }
}
