package rs.ftn.newnow.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import rs.ftn.newnow.dto.AuthResponse;
import rs.ftn.newnow.dto.CreateAccountRequestDTO;
import rs.ftn.newnow.dto.LoginRequest;
import rs.ftn.newnow.model.AccountRequest;
import rs.ftn.newnow.model.User;
import rs.ftn.newnow.model.enums.Role;
import rs.ftn.newnow.repository.AccountRequestRepository;
import rs.ftn.newnow.repository.UserRepository;
import rs.ftn.newnow.security.JwtUtil;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AccountRequestRepository accountRequestRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    private CreateAccountRequestDTO createAccountRequestDTO;
    private LoginRequest loginRequest;
    private User user;

    @BeforeEach
    void setUp() {
        createAccountRequestDTO = new CreateAccountRequestDTO();
        createAccountRequestDTO.setEmail("test@example.com");
        createAccountRequestDTO.setPassword("password123");
        createAccountRequestDTO.setName("Test User");
        createAccountRequestDTO.setAddress("Test Address");
        createAccountRequestDTO.setBirthday(LocalDate.of(1990, 1, 1));
        createAccountRequestDTO.setCity("Test City");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");

        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setPassword("encoded-password");
        user.setName("Test User");
        user.setRoles(Set.of(Role.ROLE_USER));
    }

    @Test
    void createRegistrationRequest_Success() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(accountRequestRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-password");
        
        AccountRequest savedRequest = new AccountRequest();
        savedRequest.setId(1L);
        savedRequest.setEmail("test@example.com");
        when(accountRequestRepository.save(any(AccountRequest.class))).thenReturn(savedRequest);

        AccountRequest result = authService.createRegistrationRequest(createAccountRequestDTO);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(accountRequestRepository, times(1)).save(any(AccountRequest.class));
    }

    @Test
    void createRegistrationRequest_UserAlreadyExists() {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.createRegistrationRequest(createAccountRequestDTO)
        );

        assertEquals("User with this email already exists", exception.getMessage());
        verify(accountRequestRepository, never()).save(any(AccountRequest.class));
    }

    @Test
    void createRegistrationRequest_RequestAlreadyExists() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(accountRequestRepository.existsByEmail(anyString())).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.createRegistrationRequest(createAccountRequestDTO)
        );

        assertEquals("Registration request with this email already exists", exception.getMessage());
        verify(accountRequestRepository, never()).save(any(AccountRequest.class));
    }

    @Test
    void login_Success() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken(anyString())).thenReturn("access-token");
        when(jwtUtil.generateRefreshToken(anyString())).thenReturn("refresh-token");

        AuthResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("access-token", response.getToken());
        assertEquals("refresh-token", response.getRefreshToken());
        assertEquals("test@example.com", response.getEmail());
        assertEquals("Test User", response.getName());
        assertTrue(response.getRoles().contains("ROLE_USER"));

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void login_UserNotFound() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(
                RuntimeException.class,
                () -> authService.login(loginRequest)
        );
    }

    @Test
    void refreshToken_Success() {
        String refreshToken = "valid-refresh-token";
        when(jwtUtil.extractEmail(anyString())).thenReturn("test@example.com");
        when(jwtUtil.validateToken(anyString(), anyString())).thenReturn(true);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken(anyString())).thenReturn("new-access-token");
        when(jwtUtil.generateRefreshToken(anyString())).thenReturn("new-refresh-token");

        AuthResponse response = authService.refreshToken(refreshToken);

        assertNotNull(response);
        assertEquals("new-access-token", response.getToken());
        assertEquals("new-refresh-token", response.getRefreshToken());
        assertEquals("test@example.com", response.getEmail());
    }

    @Test
    void refreshToken_InvalidToken() {
        String refreshToken = "invalid-refresh-token";
        when(jwtUtil.extractEmail(anyString())).thenReturn("test@example.com");
        when(jwtUtil.validateToken(anyString(), anyString())).thenReturn(false);

        assertThrows(
                IllegalArgumentException.class,
                () -> authService.refreshToken(refreshToken)
        );
    }

    @Test
    void refreshToken_UserNotFound() {
        String refreshToken = "valid-refresh-token";
        when(jwtUtil.extractEmail(anyString())).thenReturn("test@example.com");
        when(jwtUtil.validateToken(anyString(), anyString())).thenReturn(true);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(
                RuntimeException.class,
                () -> authService.refreshToken(refreshToken)
        );
    }
}
