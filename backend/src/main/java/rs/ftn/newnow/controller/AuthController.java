package rs.ftn.newnow.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.ftn.newnow.dto.AuthResponse;
import rs.ftn.newnow.dto.LoginRequest;
import rs.ftn.newnow.dto.RegisterRequest;
import rs.ftn.newnow.service.AuthService;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            log.info("=== REGISTER REQUEST RECEIVED for email: {} ===", request.getEmail());
            String message = authService.register(request);
            log.info("=== REGISTER SUCCESS: {} ===", message);
            return ResponseEntity.ok().body(message);
        } catch (IllegalArgumentException e) {
            log.error("=== REGISTER VALIDATION ERROR: {} ===", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("=== REGISTER FAILED ===", e);
            return ResponseEntity.internalServerError().body("Registration failed: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Login failed", e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
