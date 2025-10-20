package rs.ftn.newnow.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import rs.ftn.newnow.dto.*;
import rs.ftn.newnow.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserProfileDTO> getMyProfile(Authentication authentication) {
        try {
            log.info("Fetching profile for user: {}", authentication.getName());
            UserProfileDTO profile = userService.getUserProfile(authentication.getName());
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            log.error("Failed to fetch profile", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PatchMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateMyProfile(
            @Valid @RequestBody UpdateProfileDTO updateDTO,
            Authentication authentication) {
        try {
            log.info("Updating profile for user: {}", authentication.getName());
            UserProfileDTO profile = userService.updateUserProfile(authentication.getName(), updateDTO);
            return ResponseEntity.ok(profile);
        } catch (IllegalArgumentException e) {
            log.error("Validation error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Failed to update profile", e);
            return ResponseEntity.internalServerError().body(new MessageResponse("Failed to update profile"));
        }
    }

    @PutMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateMyAvatar(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        try {
            log.info("Updating avatar for user: {}", authentication.getName());
            String filename = userService.updateUserAvatar(authentication.getName(), file);
            return ResponseEntity.ok(new MessageResponse("Avatar updated successfully: " + filename));
        } catch (IllegalArgumentException e) {
            log.error("Validation error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Failed to update avatar", e);
            return ResponseEntity.internalServerError().body(new MessageResponse("Failed to update avatar"));
        }
    }

    @GetMapping("/me/reviews")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getMyReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "date") String sort,
            @RequestParam(defaultValue = "desc") String order,
            Authentication authentication) {
        try {
            log.info("Fetching reviews for user: {}", authentication.getName());
            Page<ReviewDTO> reviews = userService.getUserReviews(
                    authentication.getName(), page, size, sort, order);
            return ResponseEntity.ok(reviews);
        } catch (Exception e) {
            log.error("Failed to fetch reviews", e);
            return ResponseEntity.internalServerError().body(new MessageResponse("Failed to fetch reviews"));
        }
    }

    @GetMapping("/me/managed-locations")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getMyManagedLocations(Authentication authentication) {
        try {
            log.info("Fetching managed locations for user: {}", authentication.getName());
            List<ManagedLocationDTO> locations = userService.getUserManagedLocations(authentication.getName());
            return ResponseEntity.ok(locations);
        } catch (Exception e) {
            log.error("Failed to fetch managed locations", e);
            return ResponseEntity.internalServerError()
                    .body(new MessageResponse("Failed to fetch managed locations"));
        }
    }

    @PostMapping("/me/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> changePassword(
            @Valid @RequestBody ChangePasswordDTO changePasswordDTO,
            Authentication authentication) {
        try {
            log.info("Changing password for user: {}", authentication.getName());
            userService.changePassword(authentication.getName(), changePasswordDTO);
            return ResponseEntity.ok(new MessageResponse("Password changed successfully"));
        } catch (IllegalArgumentException e) {
            log.error("Validation error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Failed to change password", e);
            return ResponseEntity.internalServerError().body(new MessageResponse("Failed to change password"));
        }
    }
}
