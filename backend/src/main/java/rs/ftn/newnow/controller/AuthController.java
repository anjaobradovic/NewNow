package rs.ftn.newnow.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import rs.ftn.newnow.dto.*;
import rs.ftn.newnow.model.AccountRequest;
import rs.ftn.newnow.service.AccountRequestService;
import rs.ftn.newnow.service.AuthService;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final AccountRequestService accountRequestService;

    @PostMapping("/register-request")
    public ResponseEntity<?> createRegistrationRequest(@Valid @RequestBody CreateAccountRequestDTO request) {
        try {
            log.info("Registration request received for email: {}", request.getEmail());
            AccountRequest accountRequest = authService.createRegistrationRequest(request);
            log.info("Registration request created successfully with ID: {}", accountRequest.getId());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new MessageResponse("Registration request submitted successfully. Please wait for administrator approval."));
        } catch (IllegalArgumentException e) {
            log.error("Registration validation error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Registration failed", e);
            return ResponseEntity.internalServerError()
                    .body(new MessageResponse("Registration failed: " + e.getMessage()));
        }
    }

    @GetMapping("/register-request/{id}")
    @PreAuthorize("hasRole('ADMIN') or @accountRequestService.getRequestById(#id).email == authentication.principal.username")
    public ResponseEntity<?> getRegistrationRequest(@PathVariable Long id, Authentication authentication) {
        try {
            log.info("Fetching registration request with ID: {}", id);
            AccountRequestDTO request = accountRequestService.getRequestById(id);
            return ResponseEntity.ok(request);
        } catch (IllegalArgumentException e) {
            log.error("Error fetching request: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Failed to fetch request with id: {}", id, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse("Request not found"));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            log.info("Login request received for email: {}", request.getEmail());
            AuthResponse response = authService.login(request);
            log.info("Login successful for email: {}", request.getEmail());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Login failed for email: {}", request.getEmail(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("Invalid credentials"));
        }
    }

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> logout() {
        log.info("Logout request received");
        return ResponseEntity.ok(new MessageResponse("Logged out successfully"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        try {
            log.info("Refresh token request received");
            AuthResponse response = authService.refreshToken(request.getRefreshToken());
            log.info("Token refreshed successfully");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Invalid refresh token: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("Invalid refresh token"));
        } catch (Exception e) {
            log.error("Refresh token failed", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("Refresh token failed"));
        }
    }
}
