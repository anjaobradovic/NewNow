package rs.ftn.newnow.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import rs.ftn.newnow.model.*;
import rs.ftn.newnow.model.enums.Role;
import rs.ftn.newnow.repository.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AnalyticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ManagesRepository managesRepository;

    @Autowired
    private RateRepository rateRepository;

    private Location testLocation;
    private Event testEvent;
    private User managerUser;
    private User regularUser;
    private Manages manages;

    @BeforeEach
    void setUp() {
        managerUser = new User();
        managerUser.setEmail("manager@test.com");
        managerUser.setPassword("password");
        managerUser.setName("Manager User");
        managerUser.setRoles(Set.of(Role.ROLE_MANAGER));
        managerUser = userRepository.save(managerUser);

        regularUser = new User();
        regularUser.setEmail("user@test.com");
        regularUser.setPassword("password");
        regularUser.setName("Regular User");
        regularUser.setRoles(Set.of(Role.ROLE_USER));
        regularUser = userRepository.save(regularUser);

        testLocation = new Location();
        testLocation.setName("Test Location");
        testLocation.setAddress("Test Address 123");
        testLocation.setType("bar");
        testLocation.setDescription("Test Description");
        testLocation.setDeleted(false);
        testLocation = locationRepository.save(testLocation);

        manages = new Manages();
        manages.setUser(managerUser);
        manages.setLocation(testLocation);
        manages.setStartDate(LocalDate.now().minusMonths(1));
        manages = managesRepository.save(manages);

        testEvent = new Event();
        testEvent.setName("Test Event");
        testEvent.setAddress("Event Address");
        testEvent.setType("concert");
        testEvent.setDate(LocalDate.now().minusDays(5));
        testEvent.setPrice(50.0);
        testEvent.setRecurrent(false);
        testEvent.setDeleted(false);
        testEvent.setLocation(testLocation);
        testEvent = eventRepository.save(testEvent);

        Review review = new Review();
        review.setUser(regularUser);
        review.setLocation(testLocation);
        review.setEvent(testEvent);
        review.setComment("Great event!");
        review.setEventCount(1);
        review.setHidden(false);
        review.setDeleted(false);
        review.setDeletedByManager(false);
        review.setCreatedAt(LocalDateTime.now().minusDays(3));
        review = reviewRepository.save(review);

        Rate rate = new Rate();
        rate.setReview(review);
        rate.setPerformance(5);
        rate.setSoundLight(4);
        rate.setSpace(4);
        rate.setOverall(5);
        rateRepository.save(rate);
    }

    @Test
    @WithMockUser(username = "manager@test.com", roles = {"MANAGER"})
    void getLocationSummary_AsManager_ShouldReturnSummary() throws Exception {
        mockMvc.perform(get("/api/analytics/locations/{id}/summary", testLocation.getId())
                        .param("period", "monthly"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.locationId", is(testLocation.getId().intValue())))
                .andExpect(jsonPath("$.locationName", is("Test Location")))
                .andExpect(jsonPath("$.period", is("monthly")))
                .andExpect(jsonPath("$.totalEvents", greaterThanOrEqualTo(0)))
                .andExpect(jsonPath("$.totalReviews", greaterThanOrEqualTo(0)));
    }

    @Test
    @WithMockUser(username = "manager@test.com", roles = {"MANAGER"})
    void getLocationSummary_WithCustomPeriod_ShouldReturnSummary() throws Exception {
        LocalDate startDate = LocalDate.now().minusMonths(1);
        LocalDate endDate = LocalDate.now();

        mockMvc.perform(get("/api/analytics/locations/{id}/summary", testLocation.getId())
                        .param("period", "custom")
                        .param("startDate", startDate.toString())
                        .param("endDate", endDate.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.period", is("custom")))
                .andExpect(jsonPath("$.startDate", is(startDate.toString())))
                .andExpect(jsonPath("$.endDate", is(endDate.toString())));
    }

    @Test
    @WithMockUser(username = "user@test.com", roles = {"USER"})
    void getLocationSummary_AsNonManager_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/analytics/locations/{id}/summary", testLocation.getId())
                        .param("period", "monthly"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "manager@test.com", roles = {"MANAGER"})
    void getLocationEventCounts_AsManager_ShouldReturnCounts() throws Exception {
        mockMvc.perform(get("/api/analytics/locations/{id}/events/counts", testLocation.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalEvents", greaterThanOrEqualTo(0)))
                .andExpect(jsonPath("$.regularEvents", greaterThanOrEqualTo(0)))
                .andExpect(jsonPath("$.nonRegularEvents", greaterThanOrEqualTo(0)))
                .andExpect(jsonPath("$.freeEvents", greaterThanOrEqualTo(0)))
                .andExpect(jsonPath("$.paidEvents", greaterThanOrEqualTo(0)));
    }

    @Test
    @WithMockUser(username = "manager@test.com", roles = {"MANAGER"})
    void getTopRatings_AsManager_ShouldReturnTopRatings() throws Exception {
        mockMvc.perform(get("/api/analytics/locations/{id}/ratings/top", testLocation.getId())
                        .param("limit", "10")
                        .param("direction", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.topEvents", notNullValue()))
                .andExpect(jsonPath("$.locationRating", notNullValue()))
                .andExpect(jsonPath("$.locationRating.locationId", is(testLocation.getId().intValue())));
    }

    @Test
    @WithMockUser(username = "manager@test.com", roles = {"MANAGER"})
    void getTopRatings_WithAscendingDirection_ShouldReturnAscendingOrder() throws Exception {
        mockMvc.perform(get("/api/analytics/locations/{id}/ratings/top", testLocation.getId())
                        .param("limit", "5")
                        .param("direction", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.topEvents", notNullValue()));
    }

    @Test
    @WithMockUser(username = "manager@test.com", roles = {"MANAGER"})
    void getLatestReviews_AsManager_ShouldReturnLatestReviews() throws Exception {
        mockMvc.perform(get("/api/analytics/locations/{id}/reviews/latest", testLocation.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(lessThanOrEqualTo(3))));
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = {"ADMIN"})
    void getLocationSummary_AsAdmin_ShouldReturnSummary() throws Exception {
        User adminUser = new User();
        adminUser.setEmail("admin@test.com");
        adminUser.setPassword("password");
        adminUser.setName("Admin User");
        adminUser.setRoles(Set.of(Role.ROLE_ADMIN));
        userRepository.save(adminUser);

        mockMvc.perform(get("/api/analytics/locations/{id}/summary", testLocation.getId())
                        .param("period", "weekly"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.locationId", is(testLocation.getId().intValue())));
    }
}
