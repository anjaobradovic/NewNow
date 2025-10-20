package rs.ftn.newnow.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import rs.ftn.newnow.model.*;
import rs.ftn.newnow.repository.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class FeedControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    private Location testLocation;
    private Event todayEvent;
    private User testUser;

    @BeforeEach
    void setUp() {
        testLocation = new Location();
        testLocation.setName("Popular Location");
        testLocation.setAddress("Popular Address");
        testLocation.setType("Club");
        testLocation.setDeleted(false);
        testLocation.setTotalRating(9.5);
        testLocation = locationRepository.save(testLocation);

        todayEvent = new Event();
        todayEvent.setName("Today Event");
        todayEvent.setAddress("Event Address");
        todayEvent.setType("Party");
        todayEvent.setDate(LocalDate.now());
        todayEvent.setRecurrent(false);
        todayEvent.setDeleted(false);
        todayEvent.setLocation(testLocation);
        todayEvent.setPrice(10.0);
        todayEvent = eventRepository.save(todayEvent);

        testUser = new User();
        testUser.setEmail("feeduser@example.com");
        testUser.setPassword("password");
        testUser.setName("Feed User");
        testUser = userRepository.save(testUser);

        createReviewsForPopularLocation();
    }

    @Test
    void testGetTodayEvents_Anonymous_Success() throws Exception {
        mockMvc.perform(get("/api/feed/today-events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("Today Event"))
                .andExpect(jsonPath("$[0].date").value(LocalDate.now().toString()));
    }

    @Test
    void testGetPopularLocations_Anonymous_Success() throws Exception {
        mockMvc.perform(get("/api/feed/popular-locations")
                        .param("limit", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("Popular Location"));
    }

    @Test
    void testGetPopularLocationLatestReviews_Anonymous_Success() throws Exception {
        mockMvc.perform(get("/api/feed/popular-location-latest-reviews"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3));
    }

    private void createReviewsForPopularLocation() {
        Event pastEvent = new Event();
        pastEvent.setName("Past Regular Event");
        pastEvent.setAddress("Event Address");
        pastEvent.setType("Concert");
        pastEvent.setDate(LocalDate.now().minusDays(10));
        pastEvent.setRecurrent(true);
        pastEvent.setDeleted(false);
        pastEvent.setLocation(testLocation);
        pastEvent.setPrice(0.0);
        pastEvent = eventRepository.save(pastEvent);

        for (int i = 0; i < 3; i++) {
            Review review = new Review();
            review.setUser(testUser);
            review.setLocation(testLocation);
            review.setEvent(pastEvent);
            review.setComment("Review " + (i + 1));
            review.setEventCount(1);
            review.setHidden(false);
            review.setDeleted(false);
            review.setDeletedByManager(false);
            review.setCreatedAt(LocalDateTime.now().minusHours(i));

            Rate rate = new Rate();
            rate.setPerformance(8 + i);
            rate.setSoundLight(9);
            rate.setSpace(7);
            rate.setOverall(8 + i);
            rate.setReview(review);
            review.setRate(rate);

            reviewRepository.save(review);
        }
    }
}
