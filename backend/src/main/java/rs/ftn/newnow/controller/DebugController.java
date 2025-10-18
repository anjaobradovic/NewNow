package rs.ftn.newnow.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import rs.ftn.newnow.model.User;
import rs.ftn.newnow.repository.AdministratorRepository;
import rs.ftn.newnow.repository.UserRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/debug")
@RequiredArgsConstructor
@Slf4j
public class DebugController {

    private final UserRepository userRepository;
    private final AdministratorRepository administratorRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        List<User> users = userRepository.findAll();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/admins")
    public ResponseEntity<?> getAllAdmins() {
        long count = administratorRepository.count();
        Map<String, Object> response = new HashMap<>();
        response.put("count", count);
        response.put("admins", administratorRepository.findAll());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/check-user/{email}")
    public ResponseEntity<?> checkUser(@PathVariable String email) {
        Map<String, Object> response = new HashMap<>();
        
        User user = userRepository.findByEmail(email).orElse(null);
        
        if (user == null) {
            response.put("exists", false);
            response.put("message", "User not found with email: " + email);
            return ResponseEntity.ok(response);
        }
        
        response.put("exists", true);
        response.put("email", user.getEmail());
        response.put("name", user.getName());
        response.put("roles", user.getRoles());
        response.put("passwordHash", user.getPassword());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/verify-password/{email}")
    public ResponseEntity<?> verifyPassword(
            @PathVariable String email,
            @RequestParam String password) {
        
        Map<String, Object> response = new HashMap<>();
        
        User user = userRepository.findByEmail(email).orElse(null);
        
        if (user == null) {
            response.put("userExists", false);
            return ResponseEntity.ok(response);
        }
        
        boolean matches = passwordEncoder.matches(password, user.getPassword());
        
        response.put("userExists", true);
        response.put("email", user.getEmail());
        response.put("passwordMatches", matches);
        response.put("providedPassword", password);
        response.put("storedHash", user.getPassword());
        
        return ResponseEntity.ok(response);
    }
}
