package rs.ftn.newnow.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import rs.ftn.newnow.dto.AccountRequestDTO;
import rs.ftn.newnow.dto.ProcessAccountRequestDTO;
import rs.ftn.newnow.service.AccountRequestService;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AccountRequestService accountRequestService;

    @GetMapping("/requests/pending")
    public ResponseEntity<List<AccountRequestDTO>> getPendingRequests() {
        try {
            List<AccountRequestDTO> requests = accountRequestService.getAllPendingRequests();
            return ResponseEntity.ok(requests);
        } catch (Exception e) {
            log.error("Failed to fetch pending requests", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/requests")
    public ResponseEntity<List<AccountRequestDTO>> getAllRequests() {
        try {
            List<AccountRequestDTO> requests = accountRequestService.getAllRequests();
            return ResponseEntity.ok(requests);
        } catch (Exception e) {
            log.error("Failed to fetch all requests", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/requests/{id}")
    public ResponseEntity<?> getRequestById(@PathVariable Long id) {
        try {
            AccountRequestDTO request = accountRequestService.getRequestById(id);
            return ResponseEntity.ok(request);
        } catch (Exception e) {
            log.error("Failed to fetch request with id: {}", id, e);
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/requests/process")
    public ResponseEntity<?> processRequest(@Valid @RequestBody ProcessAccountRequestDTO processRequest) {
        try {
            log.info("=== PROCESSING REQUEST ID: {} ===", processRequest.getRequestId());
            String message = accountRequestService.processAccountRequest(processRequest);
            log.info("=== PROCESS SUCCESS: {} ===", message);
            return ResponseEntity.ok().body(message);
        } catch (IllegalArgumentException e) {
            log.error("=== PROCESS VALIDATION ERROR: {} ===", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("=== PROCESS FAILED ===", e);
            return ResponseEntity.internalServerError().body("Failed to process request: " + e.getMessage());
        }
    }
}
