package rs.ftn.newnow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import rs.ftn.newnow.dto.CreateReviewDTO;
import rs.ftn.newnow.dto.UpdateReviewDTO;
import rs.ftn.newnow.dto.CreateCommentDTO;
import rs.ftn.newnow.dto.HideReviewDTO;
import rs.ftn.newnow.model.*;
import rs.ftn.newnow.model.enums.Role;
import rs.ftn.newnow.repository.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ReviewControllerTest {

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
    private CommentRepository commentRepository;

    @Autowired
    private ManagesRepository managesRepository;

    private User testUser;
    private User managerUser;
    private Location testLocation;
    private Event testEvent;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setEmail("testuser@example.com");
        testUser.setPassword("password");
        testUser.setName("Test User");
        testUser.setRoles(Set.of(Role.ROLE_USER));
        testUser = userRepository.save(testUser);

        managerUser = new User();
        managerUser.setEmail("manager@example.com");
        managerUser.setPassword("password");
        managerUser.setName("Manager User");
        managerUser.setRoles(Set.of(Role.ROLE_USER));
        managerUser = userRepository.save(managerUser);

        testLocation = new Location();
        testLocation.setName("Test Location");
        testLocation.setAddress("Test Address");
        testLocation.setType("Club");
        testLocation.setDeleted(false);
        testLocation.setTotalRating(0.0);
        testLocation = locationRepository.save(testLocation);

        Manages manages = new Manages();
        manages.setUser(managerUser);
        manages.setLocation(testLocation);
        manages.setStartDate(LocalDate.now().minusMonths(1));
        managesRepository.save(manages);

        testEvent = new Event();
        testEvent.setName("Test Event");
        testEvent.setAddress("Event Address");
        testEvent.setType("Concert");
        testEvent.setDate(LocalDate.now().minusDays(5));
        testEvent.setRecurrent(true);
        testEvent.setDeleted(false);
        testEvent.setLocation(testLocation);
        testEvent.setPrice(0.0);
        testEvent = eventRepository.save(testEvent);
    }

    @Test
    @WithMockUser(username = "testuser@example.com")
    void testCreateReview_Success() throws Exception {
        CreateReviewDTO dto = new CreateReviewDTO();
        dto.setEventId(testEvent.getId());
        dto.setPerformance(8);
        dto.setSoundLight(9);
        dto.setSpace(7);
        dto.setOverall(8);
        dto.setComment("Great event!");

        mockMvc.perform(post("/api/locations/{locationId}/reviews", testLocation.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.comment").value("Great event!"))
                .andExpect(jsonPath("$.ratings.performance").value(8))
                .andExpect(jsonPath("$.ratings.overall").value(8));
    }

    @Test
    @WithMockUser(username = "testuser@example.com")
    void testCreateReview_NonRecurrentEvent_ShouldFail() throws Exception {
        Event nonRecurrentEvent = new Event();
        nonRecurrentEvent.setName("One Time Event");
        nonRecurrentEvent.setAddress("Event Address");
        nonRecurrentEvent.setType("Concert");
        nonRecurrentEvent.setDate(LocalDate.now().minusDays(1));
        nonRecurrentEvent.setRecurrent(false);
        nonRecurrentEvent.setDeleted(false);
        nonRecurrentEvent.setLocation(testLocation);
        nonRecurrentEvent.setPrice(0.0);
        nonRecurrentEvent = eventRepository.save(nonRecurrentEvent);

        CreateReviewDTO dto = new CreateReviewDTO();
        dto.setEventId(nonRecurrentEvent.getId());
        dto.setPerformance(8);
        dto.setSoundLight(9);
        dto.setSpace(7);
        dto.setOverall(8);

        mockMvc.perform(post("/api/locations/{locationId}/reviews", testLocation.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "testuser@example.com")
    void testCreateReview_FutureEvent_ShouldFail() throws Exception {
        Event futureEvent = new Event();
        futureEvent.setName("Future Event");
        futureEvent.setAddress("Event Address");
        futureEvent.setType("Concert");
        futureEvent.setDate(LocalDate.now().plusDays(5));
        futureEvent.setRecurrent(true);
        futureEvent.setDeleted(false);
        futureEvent.setLocation(testLocation);
        futureEvent.setPrice(0.0);
        futureEvent = eventRepository.save(futureEvent);

        CreateReviewDTO dto = new CreateReviewDTO();
        dto.setEventId(futureEvent.getId());
        dto.setPerformance(8);
        dto.setSoundLight(9);
        dto.setSpace(7);
        dto.setOverall(8);

        mockMvc.perform(post("/api/locations/{locationId}/reviews", testLocation.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetReviewDetails_Anonymous_Success() throws Exception {
        Review review = createTestReview();

        mockMvc.perform(get("/api/reviews/{id}", review.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(review.getId()))
                .andExpect(jsonPath("$.author.name").value(testUser.getName()))
                .andExpect(jsonPath("$.ratings.performance").exists());
    }

    @Test
    @WithMockUser(username = "testuser@example.com")
    void testUpdateReview_Success() throws Exception {
        Review review = createTestReview();

        UpdateReviewDTO dto = new UpdateReviewDTO();
        dto.setPerformance(10);
        dto.setSoundLight(10);
        dto.setSpace(10);
        dto.setOverall(10);
        dto.setComment("Updated comment");

        mockMvc.perform(put("/api/reviews/{id}", review.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ratings.performance").value(10))
                .andExpect(jsonPath("$.comment").value("Updated comment"));
    }

    @Test
    @WithMockUser(username = "testuser@example.com")
    void testDeleteReview_Success() throws Exception {
        Review review = createTestReview();

        mockMvc.perform(delete("/api/reviews/{id}", review.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Review deleted successfully"));
    }

    @Test
    void testGetLocationReviewsSorted_Anonymous_Success() throws Exception {
        createTestReview();
        createTestReview();

        mockMvc.perform(get("/api/locations/{locationId}/reviews/sort", testLocation.getId())
                        .param("sort", "rating")
                        .param("order", "desc")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @WithMockUser(username = "testuser@example.com")
    void testCreateComment_Success() throws Exception {
        Review review = createTestReview();

        CreateCommentDTO dto = new CreateCommentDTO();
        dto.setText("This is a comment");

        mockMvc.perform(post("/api/reviews/{id}/comments", review.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.text").value("This is a comment"))
                .andExpect(jsonPath("$.author.name").value(testUser.getName()));
    }

    @Test
    @WithMockUser(username = "testuser@example.com")
    void testCreateNestedComment_Success() throws Exception {
        Review review = createTestReview();
        Comment parentComment = createTestComment(review, null);

        CreateCommentDTO dto = new CreateCommentDTO();
        dto.setText("This is a reply");
        dto.setParentCommentId(parentComment.getId());

        mockMvc.perform(post("/api/reviews/{id}/comments", review.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.text").value("This is a reply"))
                .andExpect(jsonPath("$.parentCommentId").value(parentComment.getId()));
    }

    @Test
    void testGetComments_Anonymous_Success() throws Exception {
        Review review = createTestReview();
        createTestComment(review, null);
        createTestComment(review, null);

        mockMvc.perform(get("/api/reviews/{id}/comments", review.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @WithMockUser(username = "testuser@example.com")
    void testDeleteComment_Success() throws Exception {
        Review review = createTestReview();
        Comment comment = createTestComment(review, null);

        mockMvc.perform(delete("/api/reviews/{id}/comments/{commentId}", review.getId(), comment.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Comment deleted successfully"));
    }

    @Test
    @WithMockUser(username = "manager@example.com")
    void testHideReview_Manager_Success() throws Exception {
        Review review = createTestReview();

        HideReviewDTO dto = new HideReviewDTO();
        dto.setHidden(true);

        mockMvc.perform(patch("/api/manager/reviews/{id}/hide", review.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Review hidden successfully"));
    }

    @Test
    @WithMockUser(username = "manager@example.com")
    void testDeleteReviewByManager_Success() throws Exception {
        Review review = createTestReview();

        mockMvc.perform(delete("/api/manager/reviews/{id}", review.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Review deleted by manager successfully"));
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
        rate.setSoundLight(9);
        rate.setSpace(7);
        rate.setOverall(8);
        rate.setReview(review);
        review.setRate(rate);

        return reviewRepository.save(review);
    }

    private Comment createTestComment(Review review, Comment parentComment) {
        Comment comment = new Comment();
        comment.setText("Test comment text");
        comment.setUser(testUser);
        comment.setReview(review);
        comment.setParentComment(parentComment);
        comment.setCreatedAt(LocalDateTime.now());
        comment.setDeleted(false);
        return commentRepository.save(comment);
    }
}
