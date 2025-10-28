package rs.ftn.newnow.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import rs.ftn.newnow.dto.*;
import rs.ftn.newnow.model.AccountRequest;
import rs.ftn.newnow.model.enums.RequestStatus;
import rs.ftn.newnow.service.AccountRequestService;
import rs.ftn.newnow.service.AuthService;

import java.time.LocalDate;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private AccountRequestService accountRequestService;

    private CreateAccountRequestDTO createAccountRequestDTO;
    private LoginRequest loginRequest;
    private RefreshTokenRequest refreshTokenRequest;

    @BeforeEach
    void setUp() {
        createAccountRequestDTO = new CreateAccountRequestDTO();
        createAccountRequestDTO.setEmail("test@example.com");
        createAccountRequestDTO.setPassword("Password123!");
        createAccountRequestDTO.setName("Test User");
        createAccountRequestDTO.setAddress("Test Address");
        createAccountRequestDTO.setBirthday(LocalDate.of(1990, 1, 1));
        createAccountRequestDTO.setCity("Test City");
        createAccountRequestDTO.setPhoneNumber("+381641234567");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("Password123!");

        refreshTokenRequest = new RefreshTokenRequest();
        refreshTokenRequest.setRefreshToken("valid-refresh-token");
    }

    @Test
    void createRegistrationRequest_Success() throws Exception {
        AccountRequest accountRequest = new AccountRequest();
        accountRequest.setId(1L);
        accountRequest.setEmail("test@example.com");
        accountRequest.setStatus(RequestStatus.PENDING);

        when(authService.createRegistrationRequest(any(CreateAccountRequestDTO.class)))
                .thenReturn(accountRequest);

        mockMvc.perform(post("/api/auth/register-request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createAccountRequestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void createRegistrationRequest_InvalidEmail() throws Exception {
        createAccountRequestDTO.setEmail("invalid-email");

        mockMvc.perform(post("/api/auth/register-request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createAccountRequestDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createRegistrationRequest_DuplicateEmail() throws Exception {
        when(authService.createRegistrationRequest(any(CreateAccountRequestDTO.class)))
                .thenThrow(new IllegalArgumentException("User with this email already exists"));

        mockMvc.perform(post("/api/auth/register-request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createAccountRequestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("User with this email already exists"));
    }

    @Test
    @WithMockUser(username = "test@example.com", roles = {"USER"})
    void getRegistrationRequest_Success() throws Exception {
        AccountRequestDTO requestDTO = new AccountRequestDTO();
        requestDTO.setId(1L);
        requestDTO.setEmail("test@example.com");
        requestDTO.setName("Test User");
        requestDTO.setStatus(RequestStatus.PENDING);

        when(accountRequestService.getRequestById(anyLong())).thenReturn(requestDTO);

        mockMvc.perform(get("/api/auth/register-request/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    void login_Success() throws Exception {
        AuthResponse authResponse = new AuthResponse(
                "access-token",
                "refresh-token",
                "test@example.com",
                "Test User",
                Set.of("ROLE_USER")
        );

        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    void login_InvalidCredentials() throws Exception {
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new RuntimeException("Invalid credentials"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid credentials"));
    }

    @Test
    @WithMockUser
    void logout_Success() throws Exception {
        mockMvc.perform(post("/api/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Logged out successfully"));
    }

    @Test
    void refresh_Success() throws Exception {
        AuthResponse authResponse = new AuthResponse(
                "new-access-token",
                "new-refresh-token",
                "test@example.com",
                "Test User",
                Set.of("ROLE_USER")
        );

        when(authService.refreshToken(any(String.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshTokenRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("new-access-token"))
                .andExpect(jsonPath("$.refreshToken").value("new-refresh-token"));
    }

    @Test
    void refresh_InvalidToken() throws Exception {
        when(authService.refreshToken(any(String.class)))
                .thenThrow(new IllegalArgumentException("Invalid refresh token"));

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshTokenRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid refresh token"));
    }
}
