package rs.ftn.newnow.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import rs.ftn.newnow.dto.*;
import rs.ftn.newnow.model.*;
import rs.ftn.newnow.model.enums.Role;
import rs.ftn.newnow.repository.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ReviewServiceTest {

    @Autowired
    private ReviewService reviewService;

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

    private User testUser;
    private User managerUser;
    private Location testLocation;
    private Event testEvent;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setEmail("servicetest@example.com");
        testUser.setPassword("password");
        testUser.setName("Service Test User");
        testUser.setRoles(Set.of(Role.ROLE_USER));
        testUser = userRepository.save(testUser);

        managerUser = new User();
        managerUser.setEmail("servicemanager@example.com");
        managerUser.setPassword("password");
        managerUser.setName("Service Manager");
        managerUser.setRoles(Set.of(Role.ROLE_USER));
        managerUser = userRepository.save(managerUser);

        testLocation = new Location();
        testLocation.setName("Service Test Location");
        testLocation.setAddress("Service Address");
        testLocation.setType("Bar");
        testLocation.setDeleted(false);
        testLocation.setTotalRating(0.0);
        testLocation = locationRepository.save(testLocation);

        Manages manages = new Manages();
        manages.setUser(managerUser);
        manages.setLocation(testLocation);
        manages.setStartDate(LocalDate.now().minusMonths(1));
        managesRepository.save(manages);

        testEvent = new Event();
        testEvent.setName("Service Test Event");
        testEvent.setAddress("Event Address");
        testEvent.setType("Live Music");
        testEvent.setDate(LocalDate.now().minusDays(3));
        testEvent.setRecurrent(true);
        testEvent.setDeleted(false);
        testEvent.setLocation(testLocation);
        testEvent.setPrice(0.0);
        testEvent = eventRepository.save(testEvent);
    }

    @Test
    void testCreateReview_Success() {
        CreateReviewDTO dto = new CreateReviewDTO();
        dto.setEventId(testEvent.getId());
        dto.setPerformance(7);
        dto.setSoundAndLighting(8);
        dto.setVenue(6);
        dto.setOverallImpression(7);
        dto.setComment("Nice place");

        ReviewDetailsDTO result = reviewService.createReview(testLocation.getId(), dto, testUser.getEmail());

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("Nice place", result.getComment());
        assertEquals(7, result.getRatings().getPerformance());
        assertEquals(testUser.getName(), result.getAuthor().getName());
    }

    @Test
    void testCreateReview_DuplicateReview_ThrowsException() {
        CreateReviewDTO dto = new CreateReviewDTO();
        dto.setEventId(testEvent.getId());
        dto.setPerformance(7);
        dto.setSoundAndLighting(8);
        dto.setVenue(6);
        dto.setOverallImpression(7);

        reviewService.createReview(testLocation.getId(), dto, testUser.getEmail());

        assertThrows(RuntimeException.class, () -> {
            reviewService.createReview(testLocation.getId(), dto, testUser.getEmail());
        });
    }

    @Test
    void testGetReviewDetails_Success() {
        Review review = createTestReview();

        ReviewDetailsDTO result = reviewService.getReviewDetails(review.getId());

        assertNotNull(result);
        assertEquals(review.getId(), result.getId());
        assertEquals(review.getComment(), result.getComment());
    }

    @Test
    void testUpdateReview_Success() {
        Review review = createTestReview();

        UpdateReviewDTO dto = new UpdateReviewDTO();
        dto.setPerformance(10);
        dto.setSoundAndLighting(10);
        dto.setVenue(10);
        dto.setOverallImpression(10);
        dto.setComment("Amazing!");

        ReviewDetailsDTO result = reviewService.updateReview(review.getId(), dto, testUser.getEmail());

        assertNotNull(result);
        assertEquals(10, result.getRatings().getPerformance());
        assertEquals("Amazing!", result.getComment());
    }

    @Test
    void testDeleteReview_Success() {
        Review review = createTestReview();

        assertDoesNotThrow(() -> {
            reviewService.deleteReview(review.getId(), testUser.getEmail());
        });

        assertFalse(reviewRepository.findByIdAndNotDeleted(review.getId()).isPresent());
    }

    @Test
    void testGetLocationReviews_SortByRating() {
        createTestReview();
        createTestReview();

        Page<ReviewDetailsDTO> result = reviewService.getLocationReviews(
                testLocation.getId(), "rating", "desc", 0, 10);

        assertNotNull(result);
        assertTrue(result.hasContent());
    }

    @Test
    void testHideReview_Manager_Success() {
        Review review = createTestReview();

        assertDoesNotThrow(() -> {
            reviewService.hideReview(review.getId(), true, managerUser.getEmail());
        });

        Review updatedReview = reviewRepository.findById(review.getId()).orElseThrow();
        assertTrue(updatedReview.getHidden());
    }

    @Test
    void testDeleteReviewByManager_Success() {
        Review review = createTestReview();

        assertDoesNotThrow(() -> {
            reviewService.deleteReviewByManager(review.getId(), managerUser.getEmail());
        });

        Review deletedReview = reviewRepository.findById(review.getId()).orElseThrow();
        assertTrue(deletedReview.getDeletedByManager());
    }

    @Test
    void testHiddenReview_NotVisibleInPublicList() {
        Review review = createTestReview();
        
        // Hide the review
        reviewService.hideReview(review.getId(), true, managerUser.getEmail());
        
        // Check public list
        Page<ReviewDetailsDTO> publicReviews = reviewService.getLocationReviews(
                testLocation.getId(), "date", "desc", 0, 10);
        
        // Hidden review should not be in public list
        boolean foundInPublic = publicReviews.getContent().stream()
                .anyMatch(r -> r.getId().equals(review.getId()));
        assertFalse(foundInPublic, "Hidden review should not appear in public list");
    }

    @Test
    void testHiddenReview_VisibleInManagerList() {
        Review review = createTestReview();
        
        // Hide the review
        reviewService.hideReview(review.getId(), true, managerUser.getEmail());
        
        // Check manager list
        Page<ReviewDetailsDTO> managerReviews = reviewService.getLocationReviewsForManager(
                testLocation.getId(), "date", "desc", 0, 10);
        
        // Hidden review should be in manager list
        boolean foundInManager = managerReviews.getContent().stream()
                .anyMatch(r -> r.getId().equals(review.getId()) && r.getHidden());
        assertTrue(foundInManager, "Hidden review should appear in manager list");
    }

    private Review createTestReview() {
        Review review = new Review();
        review.setUser(testUser);
        review.setLocation(testLocation);
        review.setEvent(testEvent);
        review.setComment("Test comment");
        review.setEventCount(1);
        review.setHidden(false);
        review.setDeleted(false);
        review.setDeletedByManager(false);
        review.setCreatedAt(LocalDateTime.now());

        Rate rate = new Rate();
        rate.setPerformance(8);
        rate.setSoundAndLighting(9);
        rate.setVenue(7);
        rate.setOverallImpression(8);
        rate.setReview(review);
        review.setRate(rate);

        return reviewRepository.save(review);
    }
}
