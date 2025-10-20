package rs.ftn.newnow.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import rs.ftn.newnow.dto.*;
import rs.ftn.newnow.service.CommentService;
import rs.ftn.newnow.service.ReviewService;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final CommentService commentService;

    @PostMapping("/locations/{locationId}/reviews")
    public ResponseEntity<ReviewDetailsDTO> createReview(
            @PathVariable Long locationId,
            @Valid @RequestBody CreateReviewDTO dto,
            Authentication authentication) {
        ReviewDetailsDTO review = reviewService.createReview(locationId, dto, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(review);
    }

    @GetMapping("/reviews/{id}")
    public ResponseEntity<ReviewDetailsDTO> getReviewDetails(@PathVariable Long id) {
        ReviewDetailsDTO review = reviewService.getReviewDetails(id);
        return ResponseEntity.ok(review);
    }

    @PutMapping("/reviews/{id}")
    public ResponseEntity<ReviewDetailsDTO> updateReview(
            @PathVariable Long id,
            @Valid @RequestBody UpdateReviewDTO dto,
            Authentication authentication) {
        ReviewDetailsDTO review = reviewService.updateReview(id, dto, authentication.getName());
        return ResponseEntity.ok(review);
    }

    @DeleteMapping("/reviews/{id}")
    public ResponseEntity<MessageResponse> deleteReview(
            @PathVariable Long id,
            Authentication authentication) {
        reviewService.deleteReview(id, authentication.getName());
        return ResponseEntity.ok(new MessageResponse("Review deleted successfully"));
    }

    @GetMapping("/locations/{locationId}/reviews/sort")
    public ResponseEntity<Page<ReviewDetailsDTO>> getLocationReviewsSorted(
            @PathVariable Long locationId,
            @RequestParam(defaultValue = "date") String sort,
            @RequestParam(defaultValue = "desc") String order,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<ReviewDetailsDTO> reviews = reviewService.getLocationReviews(locationId, sort, order, page, size);
        return ResponseEntity.ok(reviews);
    }

    @PostMapping("/reviews/{id}/comments")
    public ResponseEntity<CommentDTO> createComment(
            @PathVariable Long id,
            @Valid @RequestBody CreateCommentDTO dto,
            Authentication authentication) {
        CommentDTO comment = commentService.createComment(id, dto, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(comment);
    }

    @GetMapping("/reviews/{id}/comments")
    public ResponseEntity<List<CommentDTO>> getComments(@PathVariable Long id) {
        List<CommentDTO> comments = commentService.getCommentTree(id);
        return ResponseEntity.ok(comments);
    }

    @DeleteMapping("/reviews/{id}/comments/{commentId}")
    public ResponseEntity<MessageResponse> deleteComment(
            @PathVariable Long id,
            @PathVariable Long commentId,
            Authentication authentication) {
        commentService.deleteComment(id, commentId, authentication.getName());
        return ResponseEntity.ok(new MessageResponse("Comment deleted successfully"));
    }
}
