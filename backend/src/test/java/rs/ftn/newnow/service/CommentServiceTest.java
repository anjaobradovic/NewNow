package rs.ftn.newnow.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import rs.ftn.newnow.dto.CommentDTO;
import rs.ftn.newnow.dto.CreateCommentDTO;
import rs.ftn.newnow.model.*;
import rs.ftn.newnow.repository.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CommentServiceTest {

    @Autowired
    private CommentService commentService;

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

    private User testUser;
    private Review testReview;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setEmail("commenttest@example.com");
        testUser.setPassword("password");
        testUser.setName("Comment Test User");
        testUser = userRepository.save(testUser);

        Location location = new Location();
        location.setName("Test Location");
        location.setAddress("Address");
        location.setType("Club");
        location.setDeleted(false);
        location.setTotalRating(0.0);
        location = locationRepository.save(location);

        Event event = new Event();
        event.setName("Test Event");
        event.setAddress("Event Address");
        event.setType("Concert");
        event.setDate(LocalDate.now().minusDays(1));
        event.setRecurrent(true);
        event.setDeleted(false);
        event.setLocation(location);
        event.setPrice(0.0);
        event = eventRepository.save(event);

        testReview = new Review();
        testReview.setUser(testUser);
        testReview.setLocation(location);
        testReview.setEvent(event);
        testReview.setComment("Review comment");
        testReview.setEventCount(1);
        testReview.setHidden(false);
        testReview.setDeleted(false);
        testReview.setDeletedByManager(false);
        testReview.setCreatedAt(LocalDateTime.now());

        Rate rate = new Rate();
        rate.setPerformance(8);
        rate.setSoundLight(9);
        rate.setSpace(7);
        rate.setOverall(8);
        rate.setReview(testReview);
        testReview.setRate(rate);

        testReview = reviewRepository.save(testReview);
    }

    @Test
    void testCreateComment_Success() {
        CreateCommentDTO dto = new CreateCommentDTO();
        dto.setText("This is a test comment");

        CommentDTO result = commentService.createComment(testReview.getId(), dto, testUser.getEmail());

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("This is a test comment", result.getText());
        assertEquals(testUser.getName(), result.getAuthor().getName());
    }

    @Test
    void testCreateNestedComment_Success() {
        Comment parentComment = new Comment();
        parentComment.setText("Parent comment");
        parentComment.setUser(testUser);
        parentComment.setReview(testReview);
        parentComment.setCreatedAt(LocalDateTime.now());
        parentComment.setDeleted(false);
        parentComment = commentRepository.save(parentComment);

        CreateCommentDTO dto = new CreateCommentDTO();
        dto.setText("Reply to parent");
        dto.setParentCommentId(parentComment.getId());

        CommentDTO result = commentService.createComment(testReview.getId(), dto, testUser.getEmail());

        assertNotNull(result);
        assertEquals("Reply to parent", result.getText());
        assertEquals(parentComment.getId(), result.getParentCommentId());
    }

    @Test
    void testGetCommentTree_Success() {
        Comment comment1 = createComment("Comment 1", null);
        createComment("Comment 2", null);
        createComment("Reply to Comment 1", comment1);

        List<CommentDTO> result = commentService.getCommentTree(testReview.getId());

        assertNotNull(result);
        assertEquals(2, result.size());
        
        CommentDTO firstComment = result.stream()
                .filter(c -> c.getText().equals("Comment 1"))
                .findFirst()
                .orElseThrow();
        
        assertEquals(1, firstComment.getReplies().size());
        assertEquals("Reply to Comment 1", firstComment.getReplies().get(0).getText());
    }

    @Test
    void testDeleteComment_Success() {
        Comment comment = createComment("Test comment", null);

        assertDoesNotThrow(() -> {
            commentService.deleteComment(testReview.getId(), comment.getId(), testUser.getEmail());
        });

        Comment deletedComment = commentRepository.findById(comment.getId()).orElseThrow();
        assertTrue(deletedComment.getDeleted());
    }

    private Comment createComment(String text, Comment parentComment) {
        Comment comment = new Comment();
        comment.setText(text);
        comment.setUser(testUser);
        comment.setReview(testReview);
        comment.setParentComment(parentComment);
        comment.setCreatedAt(LocalDateTime.now());
        comment.setDeleted(false);
        return commentRepository.save(comment);
    }
}
