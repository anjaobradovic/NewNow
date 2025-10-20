package rs.ftn.newnow.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import rs.ftn.newnow.dto.AccountRequestDTO;
import rs.ftn.newnow.dto.AccountRequestPageResponse;
import rs.ftn.newnow.model.AccountRequest;
import rs.ftn.newnow.model.User;
import rs.ftn.newnow.model.enums.RequestStatus;
import rs.ftn.newnow.repository.AccountRequestRepository;
import rs.ftn.newnow.repository.UserRepository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountRequestServiceTest {

    @Mock
    private AccountRequestRepository accountRequestRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AccountRequestService accountRequestService;

    private AccountRequest accountRequest;

    @BeforeEach
    void setUp() {
        accountRequest = new AccountRequest();
        accountRequest.setId(1L);
        accountRequest.setEmail("test@example.com");
        accountRequest.setPassword("encoded-password");
        accountRequest.setName("Test User");
        accountRequest.setStatus(RequestStatus.PENDING);
        accountRequest.setCreatedAt(LocalDate.now());
        accountRequest.setAddress("Test Address");
        accountRequest.setCity("Test City");
    }

    @Test
    void getFilteredRequests_AllRequests() {
        Page<AccountRequest> page = new PageImpl<>(List.of(accountRequest));
        when(accountRequestRepository.findByFilters(isNull(), isNull(), any(Pageable.class)))
                .thenReturn(page);

        AccountRequestPageResponse response = accountRequestService.getFilteredRequests(null, null, 0, 10);

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals(1, response.getTotalElements());
        verify(accountRequestRepository, times(1)).findByFilters(isNull(), isNull(), any(Pageable.class));
    }

    @Test
    void getFilteredRequests_WithStatusFilter() {
        Page<AccountRequest> page = new PageImpl<>(List.of(accountRequest));
        when(accountRequestRepository.findByFilters(eq(RequestStatus.PENDING), isNull(), any(Pageable.class)))
                .thenReturn(page);

        AccountRequestPageResponse response = accountRequestService.getFilteredRequests(
                RequestStatus.PENDING, null, 0, 10);

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
        assertEquals(RequestStatus.PENDING, response.getContent().get(0).getStatus());
    }

    @Test
    void getFilteredRequests_WithQueryFilter() {
        Page<AccountRequest> page = new PageImpl<>(List.of(accountRequest));
        when(accountRequestRepository.findByFilters(isNull(), eq("test"), any(Pageable.class)))
                .thenReturn(page);

        AccountRequestPageResponse response = accountRequestService.getFilteredRequests(
                null, "test", 0, 10);

        assertNotNull(response);
        assertEquals(1, response.getContent().size());
    }

    @Test
    void getRequestById_AdminAccess() {
        when(accountRequestRepository.findById(anyLong())).thenReturn(Optional.of(accountRequest));
        
        Collection<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
        when(authentication.getName()).thenReturn("admin@example.com");
        when(authentication.getAuthorities()).thenReturn((Collection) authorities);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        AccountRequestDTO result = accountRequestService.getRequestById(1L);

        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        verify(accountRequestRepository, times(1)).findById(1L);
    }

    @Test
    void getRequestById_OwnerAccess() {
        when(accountRequestRepository.findById(anyLong())).thenReturn(Optional.of(accountRequest));
        
        Collection<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        when(authentication.getName()).thenReturn("test@example.com");
        when(authentication.getAuthorities()).thenReturn((Collection) authorities);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        AccountRequestDTO result = accountRequestService.getRequestById(1L);

        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
    }

    @Test
    void getRequestById_AccessDenied() {
        when(accountRequestRepository.findById(anyLong())).thenReturn(Optional.of(accountRequest));
        
        Collection<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        when(authentication.getName()).thenReturn("other@example.com");
        when(authentication.getAuthorities()).thenReturn((Collection) authorities);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        assertThrows(
                IllegalArgumentException.class,
                () -> accountRequestService.getRequestById(1L)
        );
    }

    @Test
    void getRequestById_NotFound() {
        when(accountRequestRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> accountRequestService.getRequestById(999L)
        );
    }

    @Test
    void approveRequest_Success() {
        when(accountRequestRepository.findById(anyLong())).thenReturn(Optional.of(accountRequest));
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(new User());
        when(accountRequestRepository.save(any(AccountRequest.class))).thenReturn(accountRequest);
        doNothing().when(emailService).sendRegistrationApprovedEmail(anyString(), anyString());

        accountRequestService.approveRequest(1L);

        verify(userRepository, times(1)).save(any(User.class));
        verify(accountRequestRepository, times(1)).save(any(AccountRequest.class));
        verify(emailService, times(1)).sendRegistrationApprovedEmail(anyString(), anyString());
    }

    @Test
    void approveRequest_AlreadyProcessed() {
        accountRequest.setStatus(RequestStatus.ACCEPTED);
        when(accountRequestRepository.findById(anyLong())).thenReturn(Optional.of(accountRequest));

        assertThrows(
                IllegalArgumentException.class,
                () -> accountRequestService.approveRequest(1L)
        );

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void approveRequest_UserAlreadyExists() {
        when(accountRequestRepository.findById(anyLong())).thenReturn(Optional.of(accountRequest));
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        assertThrows(
                IllegalArgumentException.class,
                () -> accountRequestService.approveRequest(1L)
        );

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void rejectRequest_Success() {
        when(accountRequestRepository.findById(anyLong())).thenReturn(Optional.of(accountRequest));
        when(accountRequestRepository.save(any(AccountRequest.class))).thenReturn(accountRequest);
        doNothing().when(emailService).sendRegistrationRejectedEmail(anyString(), anyString(), anyString());

        accountRequestService.rejectRequest(1L);

        verify(accountRequestRepository, times(1)).save(any(AccountRequest.class));
        verify(emailService, times(1)).sendRegistrationRejectedEmail(anyString(), anyString(), anyString());
    }

    @Test
    void rejectRequest_AlreadyProcessed() {
        accountRequest.setStatus(RequestStatus.REJECTED);
        when(accountRequestRepository.findById(anyLong())).thenReturn(Optional.of(accountRequest));

        assertThrows(
                IllegalArgumentException.class,
                () -> accountRequestService.rejectRequest(1L)
        );

        verify(emailService, never()).sendRegistrationRejectedEmail(anyString(), anyString(), anyString());
    }

    @Test
    void rejectRequest_NotFound() {
        when(accountRequestRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> accountRequestService.rejectRequest(999L)
        );
    }
}
