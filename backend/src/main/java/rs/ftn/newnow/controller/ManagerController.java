package rs.ftn.newnow.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import rs.ftn.newnow.dto.HideReviewDTO;
import rs.ftn.newnow.dto.MessageResponse;
import rs.ftn.newnow.dto.ReviewDetailsDTO;
import rs.ftn.newnow.service.ReviewService;

@RestController
@RequestMapping("/api/manager")
@RequiredArgsConstructor
public class ManagerController {

    private final ReviewService reviewService;

    @GetMapping("/locations/{locationId}/reviews")
    public ResponseEntity<Page<ReviewDetailsDTO>> getLocationReviewsForManager(
            @PathVariable Long locationId,
            @RequestParam(defaultValue = "date") String sort,
            @RequestParam(defaultValue = "desc") String order,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {
        Page<ReviewDetailsDTO> reviews = reviewService.getLocationReviewsForManager(
                locationId, sort, order, page, size);
        return ResponseEntity.ok(reviews);
    }

    @PatchMapping("/reviews/{id}/hide")
    public ResponseEntity<MessageResponse> hideReview(
            @PathVariable Long id,
            @Valid @RequestBody HideReviewDTO dto,
            Authentication authentication) {
        reviewService.hideReview(id, dto.getHidden(), authentication.getName());
        String message = dto.getHidden() ? "Review hidden successfully" : "Review unhidden successfully";
        return ResponseEntity.ok(new MessageResponse(message));
    }

    @DeleteMapping("/reviews/{id}")
    public ResponseEntity<MessageResponse> deleteReview(
            @PathVariable Long id,
            Authentication authentication) {
        reviewService.deleteReviewByManager(id, authentication.getName());
        return ResponseEntity.ok(new MessageResponse("Review deleted by manager successfully"));
    }
}
