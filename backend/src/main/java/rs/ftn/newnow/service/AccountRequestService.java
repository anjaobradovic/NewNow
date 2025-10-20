package rs.ftn.newnow.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.ftn.newnow.dto.AccountRequestDTO;
import rs.ftn.newnow.dto.AccountRequestPageResponse;
import rs.ftn.newnow.model.AccountRequest;
import rs.ftn.newnow.model.User;
import rs.ftn.newnow.model.enums.RequestStatus;
import rs.ftn.newnow.model.enums.Role;
import rs.ftn.newnow.repository.AccountRequestRepository;
import rs.ftn.newnow.repository.UserRepository;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountRequestService {

    private final AccountRequestRepository accountRequestRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @Transactional(readOnly = true)
    public AccountRequestPageResponse getFilteredRequests(RequestStatus status, String query, int page, int size) {
        log.info("Fetching account requests with filters - status: {}, query: {}, page: {}, size: {}", 
                status, query, page, size);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<AccountRequest> requestPage = accountRequestRepository.findByFilters(status, query, pageable);
        
        return new AccountRequestPageResponse(
                requestPage.map(this::convertToDTO).getContent(),
                requestPage.getNumber(),
                requestPage.getSize(),
                requestPage.getTotalElements(),
                requestPage.getTotalPages()
        );
    }

    @Transactional(readOnly = true)
    public AccountRequestDTO getRequestById(Long id) {
        log.info("Fetching account request with id: {}", id);
        
        AccountRequest request = accountRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Account request not found with id: " + id));
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = auth.getName();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        
        if (!isAdmin && !request.getEmail().equals(currentUserEmail)) {
            throw new IllegalArgumentException("You don't have permission to view this request");
        }
        
        return convertToDTO(request);
    }

    @Transactional
    public void approveRequest(Long id) {
        log.info("Approving account request with id: {}", id);

        AccountRequest accountRequest = accountRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Account request not found with id: " + id));

        if (accountRequest.getStatus() != RequestStatus.PENDING) {
            throw new IllegalArgumentException("This request has already been processed");
        }

        if (userRepository.existsByEmail(accountRequest.getEmail())) {
            throw new IllegalArgumentException("User with this email already exists");
        }

        User user = new User();
        user.setEmail(accountRequest.getEmail());
        user.setPassword(accountRequest.getPassword());
        user.setName(accountRequest.getName());
        user.setPhoneNumber(accountRequest.getPhoneNumber());
        user.setBirthday(accountRequest.getBirthday());
        user.setAddress(accountRequest.getAddress());
        user.setCity(accountRequest.getCity());
        
        Set<Role> roles = new HashSet<>();
        roles.add(Role.ROLE_USER);
        user.setRoles(roles);

        userRepository.save(user);

        accountRequest.setStatus(RequestStatus.ACCEPTED);
        accountRequestRepository.save(accountRequest);

        emailService.sendRegistrationApprovedEmail(accountRequest.getEmail(), accountRequest.getName());

        log.info("Account request approved for email: {}", accountRequest.getEmail());
    }

    @Transactional
    public void rejectRequest(Long id) {
        log.info("Rejecting account request with id: {}", id);
        
        AccountRequest accountRequest = accountRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Account request not found with id: " + id));

        if (accountRequest.getStatus() != RequestStatus.PENDING) {
            throw new IllegalArgumentException("This request has already been processed");
        }

        accountRequest.setStatus(RequestStatus.REJECTED);
        accountRequestRepository.save(accountRequest);

        emailService.sendRegistrationRejectedEmail(
                accountRequest.getEmail(), 
                accountRequest.getName(), 
                "Your registration request has been rejected by the administrator."
        );

        log.info("Account request rejected for email: {}", accountRequest.getEmail());
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
