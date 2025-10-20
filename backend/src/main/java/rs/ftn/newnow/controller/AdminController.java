package rs.ftn.newnow.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import rs.ftn.newnow.dto.AccountRequestPageResponse;
import rs.ftn.newnow.dto.AssignManagerDTO;
import rs.ftn.newnow.dto.ManagerDTO;
import rs.ftn.newnow.dto.MessageResponse;
import rs.ftn.newnow.model.enums.RequestStatus;
import rs.ftn.newnow.service.AccountRequestService;
import rs.ftn.newnow.service.ManagesService;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AccountRequestService accountRequestService;
    private final ManagesService managesService;

    @GetMapping("/register-requests")
    public ResponseEntity<?> getRegisterRequests(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            log.info("Fetching register requests - status: {}, query: {}, page: {}, size: {}", 
                    status, q, page, size);
            
            RequestStatus requestStatus = null;
            if (status != null && !status.isEmpty()) {
                try {
                    requestStatus = RequestStatus.valueOf(status.toUpperCase());
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.badRequest()
                            .body(new MessageResponse("Invalid status value. Allowed: pending, accepted, rejected"));
                }
            }
            
            AccountRequestPageResponse response = accountRequestService.getFilteredRequests(
                    requestStatus, q, page, size);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to fetch register requests", e);
            return ResponseEntity.internalServerError()
                    .body(new MessageResponse("Failed to fetch requests"));
        }
    }

    @PatchMapping("/register-requests/{id}/approve")
    public ResponseEntity<?> approveRequest(@PathVariable Long id) {
        try {
            log.info("Approving register request with ID: {}", id);
            accountRequestService.approveRequest(id);
            return ResponseEntity.ok(new MessageResponse("Account request approved successfully"));
        } catch (IllegalArgumentException e) {
            log.error("Validation error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Failed to approve request", e);
            return ResponseEntity.internalServerError()
                    .body(new MessageResponse("Failed to approve request"));
        }
    }

    @PatchMapping("/register-requests/{id}/reject")
    public ResponseEntity<?> rejectRequest(@PathVariable Long id) {
        try {
            log.info("Rejecting register request with ID: {}", id);
            accountRequestService.rejectRequest(id);
            return ResponseEntity.ok(new MessageResponse("Account request rejected successfully"));
        } catch (IllegalArgumentException e) {
            log.error("Validation error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Failed to reject request", e);
            return ResponseEntity.internalServerError()
                    .body(new MessageResponse("Failed to reject request"));
        }
    }

    @GetMapping("/locations/{id}/managers")
    public ResponseEntity<?> getLocationManagers(@PathVariable Long id) {
        try {
            log.info("Fetching managers for location ID: {}", id);
            List<ManagerDTO> managers = managesService.getLocationManagers(id);
            return ResponseEntity.ok(managers);
        } catch (IllegalArgumentException e) {
            log.error("Validation error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Failed to fetch location managers", e);
            return ResponseEntity.internalServerError()
                    .body(new MessageResponse("Failed to fetch managers"));
        }
    }

    @PostMapping("/locations/{id}/managers")
    public ResponseEntity<?> assignManager(@PathVariable Long id, 
                                           @Validated @RequestBody AssignManagerDTO assignManagerDTO) {
        try {
            log.info("Assigning manager to location ID: {}", id);
            managesService.assignManager(id, assignManagerDTO);
            return ResponseEntity.ok(new MessageResponse("Manager assigned successfully"));
        } catch (IllegalArgumentException e) {
            log.error("Validation error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Failed to assign manager", e);
            return ResponseEntity.internalServerError()
                    .body(new MessageResponse("Failed to assign manager"));
        }
    }

    @DeleteMapping("/locations/{id}/managers/{userId}")
    public ResponseEntity<?> removeManager(@PathVariable Long id, @PathVariable Long userId) {
        try {
            log.info("Removing manager {} from location ID: {}", userId, id);
            managesService.removeManager(id, userId);
            return ResponseEntity.ok(new MessageResponse("Manager removed successfully"));
        } catch (IllegalArgumentException e) {
            log.error("Validation error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Failed to remove manager", e);
            return ResponseEntity.internalServerError()
                    .body(new MessageResponse("Failed to remove manager"));
        }
    }
}
