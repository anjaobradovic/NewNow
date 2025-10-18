package rs.ftn.newnow.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.ftn.newnow.dto.AccountRequestDTO;
import rs.ftn.newnow.dto.ProcessAccountRequestDTO;
import rs.ftn.newnow.model.AccountRequest;
import rs.ftn.newnow.model.User;
import rs.ftn.newnow.model.enums.RequestStatus;
import rs.ftn.newnow.model.enums.Role;
import rs.ftn.newnow.repository.AccountRequestRepository;
import rs.ftn.newnow.repository.UserRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountRequestService {

    private final AccountRequestRepository accountRequestRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @Transactional(readOnly = true)
    public List<AccountRequestDTO> getAllPendingRequests() {
        log.info("Fetching all pending account requests");
        return accountRequestRepository.findByStatus(RequestStatus.PENDING)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AccountRequestDTO> getAllRequests() {
        log.info("Fetching all account requests");
        return accountRequestRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AccountRequestDTO getRequestById(Long id) {
        log.info("Fetching account request with id: {}", id);
        AccountRequest request = accountRequestRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account request not found with id: " + id));
        return convertToDTO(request);
    }

    @Transactional
    public String processAccountRequest(ProcessAccountRequestDTO processRequest) {
        log.info("Processing account request id: {}, approved: {}", 
                processRequest.getRequestId(), processRequest.getApproved());

        AccountRequest accountRequest = accountRequestRepository.findById(processRequest.getRequestId())
                .orElseThrow(() -> new RuntimeException("Account request not found with id: " + processRequest.getRequestId()));

        if (accountRequest.getStatus() != RequestStatus.PENDING) {
            throw new RuntimeException("This request has already been processed");
        }

        if (processRequest.getApproved()) {
            return approveRequest(accountRequest);
        } else {
            return rejectRequest(accountRequest, processRequest.getRejectionReason());
        }
    }

    private String approveRequest(AccountRequest accountRequest) {
        // Check if user already exists
        if (userRepository.existsByEmail(accountRequest.getEmail())) {
            throw new RuntimeException("User with this email already exists");
        }

        // Create new user from account request
        User user = new User();
        user.setEmail(accountRequest.getEmail());
        user.setPassword(accountRequest.getPassword()); // Already encoded
        user.setName(accountRequest.getName());
        user.setPhoneNumber(accountRequest.getPhoneNumber());
        user.setBirthday(accountRequest.getBirthday());
        user.setAddress(accountRequest.getAddress());
        user.setCity(accountRequest.getCity());
        
        Set<Role> roles = new HashSet<>();
        roles.add(Role.ROLE_USER);
        user.setRoles(roles);

        userRepository.save(user);

        // Update account request status
        accountRequest.setStatus(RequestStatus.ACCEPTED);
        accountRequestRepository.save(accountRequest);

        // Send approval email
        emailService.sendRegistrationApprovedEmail(accountRequest.getEmail(), accountRequest.getName());

        log.info("Account request approved for email: {}", accountRequest.getEmail());
        return "Account request approved successfully. User can now log in.";
    }

    private String rejectRequest(AccountRequest accountRequest, String rejectionReason) {
        accountRequest.setStatus(RequestStatus.REJECTED);
        accountRequest.setRejectionReason(rejectionReason);
        accountRequestRepository.save(accountRequest);

        // Send rejection email
        emailService.sendRegistrationRejectedEmail(
                accountRequest.getEmail(), 
                accountRequest.getName(), 
                rejectionReason
        );

        log.info("Account request rejected for email: {}", accountRequest.getEmail());
        return "Account request rejected successfully.";
    }

    private AccountRequestDTO convertToDTO(AccountRequest request) {
        AccountRequestDTO dto = new AccountRequestDTO();
        dto.setId(request.getId());
        dto.setEmail(request.getEmail());
        dto.setName(request.getName());
        dto.setPhoneNumber(request.getPhoneNumber());
        dto.setBirthday(request.getBirthday());
        dto.setAddress(request.getAddress());
        dto.setCity(request.getCity());
        dto.setStatus(request.getStatus());
        dto.setCreatedAt(request.getCreatedAt());
        dto.setRejectionReason(request.getRejectionReason());
        return dto;
    }
}
