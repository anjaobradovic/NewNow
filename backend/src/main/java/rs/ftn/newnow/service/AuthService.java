package rs.ftn.newnow.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.ftn.newnow.dto.AuthResponse;
import rs.ftn.newnow.dto.CreateAccountRequestDTO;
import rs.ftn.newnow.dto.LoginRequest;
import rs.ftn.newnow.model.AccountRequest;
import rs.ftn.newnow.model.User;
import rs.ftn.newnow.repository.AccountRequestRepository;
import rs.ftn.newnow.repository.UserRepository;
import rs.ftn.newnow.security.JwtUtil;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final AccountRequestRepository accountRequestRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AccountRequest createRegistrationRequest(CreateAccountRequestDTO request) {
        log.info("Processing registration request for email: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("User with this email already exists");
        }

        if (accountRequestRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Registration request with this email already exists");
        }

        AccountRequest accountRequest = new AccountRequest();
        accountRequest.setEmail(request.getEmail());
        accountRequest.setPassword(passwordEncoder.encode(request.getPassword()));
        accountRequest.setName(request.getName());
        accountRequest.setPhoneNumber(request.getPhoneNumber());
        accountRequest.setBirthday(request.getBirthday());
        accountRequest.setAddress(request.getAddress());
        accountRequest.setCity(request.getCity());

        accountRequest = accountRequestRepository.save(accountRequest);
        
        log.info("Registration request created successfully with ID: {}", accountRequest.getId());
        return accountRequest;
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        log.info("Processing login request for email: {}", request.getEmail());

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = jwtUtil.generateToken(user.getEmail());
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());

        log.info("User logged in successfully: {}", request.getEmail());

        return new AuthResponse(
                token,
                refreshToken,
                user.getEmail(),
                user.getName(),
                user.getRoles().stream().map(Enum::name).collect(Collectors.toSet())
        );
    }
    
    @Transactional(readOnly = true)
    public AuthResponse refreshToken(String refreshToken) {
        log.info("Processing refresh token request");
        
        String email = jwtUtil.extractEmail(refreshToken);
        
        if (!jwtUtil.validateToken(refreshToken, email)) {
            throw new IllegalArgumentException("Invalid refresh token");
        }
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        String newToken = jwtUtil.generateToken(user.getEmail());
        String newRefreshToken = jwtUtil.generateRefreshToken(user.getEmail());
        
        log.info("Token refreshed successfully for user: {}", email);
        
        return new AuthResponse(
                newToken,
                newRefreshToken,
                user.getEmail(),
                user.getName(),
                user.getRoles().stream().map(Enum::name).collect(Collectors.toSet())
        );
    }
}
