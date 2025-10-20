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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;
import rs.ftn.newnow.dto.*;
import rs.ftn.newnow.model.*;
import rs.ftn.newnow.model.enums.Role;
import rs.ftn.newnow.repository.ImageRepository;
import rs.ftn.newnow.repository.ManagesRepository;
import rs.ftn.newnow.repository.ReviewRepository;
import rs.ftn.newnow.repository.UserRepository;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ManagesRepository managesRepository;

    @Mock
    private ImageRepository imageRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private String testEmail = "test@example.com";

    @BeforeEach
    void setUp() {
        userService.uploadDir = "uploads/avatars";
        
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail(testEmail);
        testUser.setName("Test User");
        testUser.setPassword("encodedPassword");
        testUser.setPhoneNumber("123456789");
        testUser.setBirthday(LocalDate.of(1990, 1, 1));
        testUser.setAddress("Test Address");
        testUser.setCity("Test City");
        testUser.setCreatedAt(LocalDate.now());
        testUser.setRoles(new HashSet<>(Collections.singletonList(Role.ROLE_USER)));
    }

    @Test
    void getUserProfile_Success() {
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));

        UserProfileDTO result = userService.getUserProfile(testEmail);

        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        assertEquals(testUser.getEmail(), result.getEmail());
        assertEquals(testUser.getName(), result.getName());
        verify(userRepository).findByEmail(testEmail);
    }

    @Test
    void getUserProfile_UserNotFound() {
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userService.getUserProfile(testEmail));
        verify(userRepository).findByEmail(testEmail);
    }

    @Test
    void updateUserProfile_Success() {
        UpdateProfileDTO updateDTO = new UpdateProfileDTO();
        updateDTO.setName("Updated Name");
        updateDTO.setPhoneNumber("987654321");
        updateDTO.setCity("Updated City");

        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserProfileDTO result = userService.updateUserProfile(testEmail, updateDTO);

        assertNotNull(result);
        assertEquals("Updated Name", testUser.getName());
        assertEquals("987654321", testUser.getPhoneNumber());
        assertEquals("Updated City", testUser.getCity());
        verify(userRepository).save(testUser);
    }

    @Test
    void updateUserAvatar_Success() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getContentType()).thenReturn("image/jpeg");
        when(file.getOriginalFilename()).thenReturn("avatar.jpg");
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[100]));

        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(imageRepository.findByUserId(testUser.getId())).thenReturn(Optional.empty());
        when(imageRepository.save(any(Image.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String result = userService.updateUserAvatar(testEmail, file);

        assertNotNull(result);
        assertTrue(result.endsWith(".jpg"));
        verify(imageRepository).save(any(Image.class));
    }

    @Test
    void updateUserAvatar_EmptyFile() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(true);

        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));

        assertThrows(IllegalArgumentException.class, 
                () -> userService.updateUserAvatar(testEmail, file));
    }

    @Test
    void updateUserAvatar_InvalidFileType() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getContentType()).thenReturn("application/pdf");

        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));

        assertThrows(IllegalArgumentException.class, 
                () -> userService.updateUserAvatar(testEmail, file));
    }

    @Test
    void getUserReviews_Success() {
        Location location = new Location();
        location.setId(1L);
        location.setName("Test Location");

        Event event = new Event();
        event.setId(1L);
        event.setName("Test Event");

        Review review = new Review();
        review.setId(1L);
        review.setCreatedAt(LocalDateTime.now());
        review.setEventCount(1);
        review.setHidden(false);
        review.setUser(testUser);
        review.setLocation(location);
        review.setEvent(event);

        Rate rate = new Rate();
        rate.setId(1L);
        rate.setPerformance(5);
        rate.setSoundAndLighting(4);
        rate.setVenue(5);
        rate.setOverallImpression(5);
        review.setRate(rate);

        Page<Review> reviewPage = new PageImpl<>(Collections.singletonList(review));

        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(reviewRepository.findByUserIdAndDeletedFalse(eq(testUser.getId()), any(Pageable.class)))
                .thenReturn(reviewPage);

        Page<ReviewDTO> result = userService.getUserReviews(testEmail, 0, 10, "date", "desc");

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Test Location", result.getContent().get(0).getLocationName());
        verify(reviewRepository).findByUserIdAndDeletedFalse(eq(testUser.getId()), any(Pageable.class));
    }

    @Test
    void getUserManagedLocations_Success() {
        Location location = new Location();
        location.setId(1L);
        location.setName("Managed Location");
        location.setAddress("Location Address");
        location.setType("Restaurant");

        Manages manages = new Manages();
        manages.setId(1L);
        manages.setUser(testUser);
        manages.setLocation(location);
        manages.setStartDate(LocalDate.now().minusMonths(1));
        manages.setEndDate(null);

        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(managesRepository.findActiveByUserId(eq(testUser.getId()), any(LocalDate.class)))
                .thenReturn(Collections.singletonList(manages));

        List<ManagedLocationDTO> result = userService.getUserManagedLocations(testEmail);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Managed Location", result.get(0).getLocationName());
        assertTrue(result.get(0).getIsActive());
        verify(managesRepository).findActiveByUserId(eq(testUser.getId()), any(LocalDate.class));
    }

    @Test
    void changePassword_Success() {
        ChangePasswordDTO changePasswordDTO = new ChangePasswordDTO();
        changePasswordDTO.setCurrentPassword("currentPassword");
        changePasswordDTO.setNewPassword("newPassword123");
        changePasswordDTO.setConfirmPassword("newPassword123");

        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("currentPassword", testUser.getPassword())).thenReturn(true);
        when(passwordEncoder.encode("newPassword123")).thenReturn("encodedNewPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.changePassword(testEmail, changePasswordDTO);

        verify(passwordEncoder).encode("newPassword123");
        verify(userRepository).save(testUser);
        verify(emailService).sendPasswordChangeEmail(testUser.getEmail(), testUser.getName());
    }

    @Test
    void changePassword_WrongCurrentPassword() {
        ChangePasswordDTO changePasswordDTO = new ChangePasswordDTO();
        changePasswordDTO.setCurrentPassword("wrongPassword");
        changePasswordDTO.setNewPassword("newPassword123");
        changePasswordDTO.setConfirmPassword("newPassword123");

        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongPassword", testUser.getPassword())).thenReturn(false);

        assertThrows(IllegalArgumentException.class, 
                () -> userService.changePassword(testEmail, changePasswordDTO));
        verify(userRepository, never()).save(any());
        verify(emailService, never()).sendPasswordChangeEmail(anyString(), anyString());
    }

    @Test
    void changePassword_PasswordMismatch() {
        ChangePasswordDTO changePasswordDTO = new ChangePasswordDTO();
        changePasswordDTO.setCurrentPassword("currentPassword");
        changePasswordDTO.setNewPassword("newPassword123");
        changePasswordDTO.setConfirmPassword("differentPassword");

        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("currentPassword", testUser.getPassword())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, 
                () -> userService.changePassword(testEmail, changePasswordDTO));
        verify(userRepository, never()).save(any());
        verify(emailService, never()).sendPasswordChangeEmail(anyString(), anyString());
    }

    @Test
    void changePassword_SameAsCurrentPassword() {
        ChangePasswordDTO changePasswordDTO = new ChangePasswordDTO();
        changePasswordDTO.setCurrentPassword("currentPassword");
        changePasswordDTO.setNewPassword("currentPassword");
        changePasswordDTO.setConfirmPassword("currentPassword");

        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("currentPassword", testUser.getPassword())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, 
                () -> userService.changePassword(testEmail, changePasswordDTO));
        verify(userRepository, never()).save(any());
        verify(emailService, never()).sendPasswordChangeEmail(anyString(), anyString());
    }
}
