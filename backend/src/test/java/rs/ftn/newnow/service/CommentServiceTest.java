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

    @Autowired
    private ManagesRepository managesRepository;

    private User testUser;
    private User managerUser;
    private User regularUser;
    private Location testLocation;
    private Review testReview;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setEmail("commenttest@example.com");
        testUser.setPassword("password");
        testUser.setName("Comment Test User");
        testUser = userRepository.save(testUser);

        managerUser = new User();
        managerUser.setEmail("manager@example.com");
        managerUser.setPassword("password");
        managerUser.setName("Manager User");
        managerUser = userRepository.save(managerUser);

        regularUser = new User();
        regularUser.setEmail("regular@example.com");
        regularUser.setPassword("password");
        regularUser.setName("Regular User");
        regularUser = userRepository.save(regularUser);

        testLocation = new Location();
        testLocation.setName("Test Location");
        testLocation.setAddress("Address");
        testLocation.setType("Club");
        testLocation.setDeleted(false);
        testLocation.setTotalRating(0.0);
        testLocation = locationRepository.save(testLocation);

        // Make managerUser a manager of testLocation
        Manages manages = new Manages();
        manages.setUser(managerUser);
        manages.setLocation(testLocation);
        manages.setStartDate(LocalDate.now().minusMonths(1));
        managesRepository.save(manages);

        Event event = new Event();
        event.setName("Test Event");
        event.setAddress("Event Address");
        event.setType("Concert");
        event.setDate(LocalDate.now().minusDays(1));
        event.setRecurrent(true);
        event.setDeleted(false);
        event.setLocation(testLocation);
        event.setPrice(0.0);
        event = eventRepository.save(event);

        testReview = new Review();
        testReview.setUser(testUser);
        testReview.setLocation(testLocation);
        testReview.setEvent(event);
        testReview.setComment("Review comment");
        testReview.setEventCount(1);
        testReview.setHidden(false);
        testReview.setDeleted(false);
        testReview.setDeletedByManager(false);
        testReview.setCreatedAt(LocalDateTime.now());

        Rate rate = new Rate();
        rate.setPerformance(8);
        rate.setSoundAndLighting(9);
        rate.setVenue(7);
        rate.setOverallImpression(8);
        rate.setReview(testReview);
        testReview.setRate(rate);

        testReview = reviewRepository.save(testReview);
    }

    @Test
    void testCreateComment_Success() {
        // Manager can comment directly
        CreateCommentDTO dto = new CreateCommentDTO();
        dto.setText("This is a test comment");

        CommentDTO result = commentService.createComment(testReview.getId(), dto, managerUser.getEmail());

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("This is a test comment", result.getText());
        assertEquals(managerUser.getName(), result.getAuthor().getName());
    }

    @Test
    void testCreateNestedComment_Success() {
        // Manager creates root comment
        Comment parentComment = new Comment();
        parentComment.setText("Manager Parent comment");
        parentComment.setUser(managerUser);
        parentComment.setReview(testReview);
        parentComment.setCreatedAt(LocalDateTime.now());
        parentComment.setDeleted(false);
        parentComment = commentRepository.save(parentComment);

        // Regular user can reply to manager's comment
        CreateCommentDTO dto = new CreateCommentDTO();
        dto.setText("Reply to parent");
        dto.setParentCommentId(parentComment.getId());

        CommentDTO result = commentService.createComment(testReview.getId(), dto, regularUser.getEmail());

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

    @Test
    void testOnlyManagerCanCommentDirectlyOnReview() {
        // Manager can comment directly
        CreateCommentDTO managerDto = new CreateCommentDTO();
        managerDto.setText("Manager comment");
        
        assertDoesNotThrow(() -> {
            commentService.createComment(testReview.getId(), managerDto, managerUser.getEmail());
        });

        // Regular user cannot comment directly
        CreateCommentDTO regularDto = new CreateCommentDTO();
        regularDto.setText("Regular user comment");
        
        Exception exception = assertThrows(RuntimeException.class, () -> {
            commentService.createComment(testReview.getId(), regularDto, regularUser.getEmail());
        });
        
        assertTrue(exception.getMessage().contains("Only managers can comment directly on reviews"));
    }

    @Test
    void testRegularUserCanReplyToManagerComment() {
        // Manager posts a comment
        CreateCommentDTO managerDto = new CreateCommentDTO();
        managerDto.setText("Manager comment");
        CommentDTO managerComment = commentService.createComment(
                testReview.getId(), managerDto, managerUser.getEmail());
        
        // Regular user can reply to manager's comment
        CreateCommentDTO replyDto = new CreateCommentDTO();
        replyDto.setText("Reply to manager");
        replyDto.setParentCommentId(managerComment.getId());
        
        assertDoesNotThrow(() -> {
            commentService.createComment(testReview.getId(), replyDto, regularUser.getEmail());
        });
    }

    @Test
    void testRegularUserCannotReplyToRegularUserComment() {
        // Create a root level comment by another regular user (this should fail in new logic)
        // But let's assume there's an old comment somehow
        Comment rootComment = new Comment();
        rootComment.setText("Some comment");
        rootComment.setUser(regularUser);
        rootComment.setReview(testReview);
        rootComment.setCreatedAt(LocalDateTime.now());
        rootComment.setDeleted(false);
        rootComment = commentRepository.save(rootComment);
        
        // Another regular user tries to reply to this root comment
        CreateCommentDTO replyDto = new CreateCommentDTO();
        replyDto.setText("Reply to regular user");
        replyDto.setParentCommentId(rootComment.getId());
        
        Exception exception = assertThrows(RuntimeException.class, () -> {
            commentService.createComment(testReview.getId(), replyDto, testUser.getEmail());
        });
        
        assertTrue(exception.getMessage().contains("You can only reply to manager's comments at this level"));
    }

    @Test
    void testNestedRepliesAllowed() {
        // Manager posts a comment
        CreateCommentDTO managerDto = new CreateCommentDTO();
        managerDto.setText("Manager comment");
        CommentDTO managerComment = commentService.createComment(
                testReview.getId(), managerDto, managerUser.getEmail());
        
        // Regular user replies to manager
        CreateCommentDTO reply1Dto = new CreateCommentDTO();
        reply1Dto.setText("Reply to manager");
        reply1Dto.setParentCommentId(managerComment.getId());
        CommentDTO reply1 = commentService.createComment(
                testReview.getId(), reply1Dto, regularUser.getEmail());
        
        // Another user replies to the reply (third level - anyone can reply)
        CreateCommentDTO reply2Dto = new CreateCommentDTO();
        reply2Dto.setText("Reply to reply");
        reply2Dto.setParentCommentId(reply1.getId());
        
        assertDoesNotThrow(() -> {
            commentService.createComment(testReview.getId(), reply2Dto, testUser.getEmail());
        });
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
