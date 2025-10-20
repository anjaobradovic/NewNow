package rs.ftn.newnow.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import rs.ftn.newnow.dto.*;
import rs.ftn.newnow.model.Image;
import rs.ftn.newnow.model.Manages;
import rs.ftn.newnow.model.Review;
import rs.ftn.newnow.model.User;
import rs.ftn.newnow.repository.ImageRepository;
import rs.ftn.newnow.repository.ManagesRepository;
import rs.ftn.newnow.repository.ReviewRepository;
import rs.ftn.newnow.repository.UserRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final ManagesRepository managesRepository;
    private final ImageRepository imageRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Value("${file.upload.dir:uploads/avatars}")
    String uploadDir;

    @Transactional(readOnly = true)
    public UserProfileDTO getUserProfile(String email) {
        log.info("Fetching profile for user: {}", email);
        User user = findUserByEmail(email);
        return mapToUserProfileDTO(user);
    }

    @Transactional
    public UserProfileDTO updateUserProfile(String email, UpdateProfileDTO updateDTO) {
        log.info("Updating profile for user: {}", email);
        User user = findUserByEmail(email);

        if (updateDTO.getName() != null) {
            user.setName(updateDTO.getName());
        }
        if (updateDTO.getPhoneNumber() != null) {
            user.setPhoneNumber(updateDTO.getPhoneNumber());
        }
        if (updateDTO.getBirthday() != null) {
            user.setBirthday(updateDTO.getBirthday());
        }
        if (updateDTO.getAddress() != null) {
            user.setAddress(updateDTO.getAddress());
        }
        if (updateDTO.getCity() != null) {
            user.setCity(updateDTO.getCity());
        }

        user = userRepository.save(user);
        log.info("Profile updated successfully for user: {}", email);
        return mapToUserProfileDTO(user);
    }

    @Transactional
    public String updateUserAvatar(String email, MultipartFile file) {
        log.info("Updating avatar for user: {}", email);
        User user = findUserByEmail(email);

        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("File must be an image");
        }

        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : ".jpg";
            String filename = UUID.randomUUID().toString() + extension;
            Path filePath = uploadPath.resolve(filename);

            Files.copy(file.getInputStream(), filePath);
            log.info("Avatar file saved: {}", filename);

            Image existingImage = imageRepository.findByUserId(user.getId()).orElse(null);
            if (existingImage != null) {
                deleteAvatarFile(existingImage.getPath());
                existingImage.setPath(filename);
                imageRepository.save(existingImage);
            } else {
                Image newImage = new Image();
                newImage.setPath(filename);
                newImage.setUser(user);
                imageRepository.save(newImage);
            }

            log.info("Avatar updated successfully for user: {}", email);
            return filename;
        } catch (IOException e) {
            log.error("Failed to save avatar file", e);
            throw new RuntimeException("Failed to save avatar file", e);
        }
    }

    @Transactional(readOnly = true)
    public Page<ReviewDTO> getUserReviews(String email, int page, int size, String sortBy, String order) {
        log.info("Fetching reviews for user: {} (page={}, size={}, sortBy={}, order={})",
                email, page, size, sortBy, order);
        User user = findUserByEmail(email);

        Sort.Direction direction = "asc".equalsIgnoreCase(order) ? Sort.Direction.ASC : Sort.Direction.DESC;
        String sortField = "rating".equals(sortBy) ? "rate.overallImpression" : "createdAt";
        Sort sort = Sort.by(direction, sortField);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Review> reviews = reviewRepository.findByUserIdAndDeletedFalse(user.getId(), pageable);
        return reviews.map(this::mapToReviewDTO);
    }

    @Transactional(readOnly = true)
    public List<ManagedLocationDTO> getUserManagedLocations(String email) {
        log.info("Fetching managed locations for user: {}", email);
        User user = findUserByEmail(email);

        List<Manages> manages = managesRepository.findActiveByUserId(user.getId(), LocalDate.now());
        return manages.stream()
                .map(this::mapToManagedLocationDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void changePassword(String email, ChangePasswordDTO changePasswordDTO) {
        log.info("Changing password for user: {}", email);
        User user = findUserByEmail(email);

        if (!passwordEncoder.matches(changePasswordDTO.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

        if (!changePasswordDTO.getNewPassword().equals(changePasswordDTO.getConfirmPassword())) {
            throw new IllegalArgumentException("New password and confirmation do not match");
        }

        if (changePasswordDTO.getCurrentPassword().equals(changePasswordDTO.getNewPassword())) {
            throw new IllegalArgumentException("New password must be different from current password");
        }

        user.setPassword(passwordEncoder.encode(changePasswordDTO.getNewPassword()));
        userRepository.save(user);

        emailService.sendPasswordChangeEmail(user.getEmail(), user.getName());
        log.info("Password changed successfully for user: {}", email);
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private UserProfileDTO mapToUserProfileDTO(User user) {
        UserProfileDTO dto = new UserProfileDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setName(user.getName());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setBirthday(user.getBirthday());
        dto.setAddress(user.getAddress());
        dto.setCity(user.getCity());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setRoles(user.getRoles().stream().map(Enum::name).collect(Collectors.toSet()));

        if (user.getProfileImage() != null) {
            dto.setAvatarUrl("/api/avatars/" + user.getProfileImage().getPath());
        }

        return dto;
    }

    private ReviewDTO mapToReviewDTO(Review review) {
        ReviewDTO dto = new ReviewDTO();
        dto.setId(review.getId());
        dto.setCreatedAt(review.getCreatedAt());
        dto.setEventCount(review.getEventCount());
        dto.setHidden(review.getHidden());
        dto.setLocationId(review.getLocation().getId());
        dto.setLocationName(review.getLocation().getName());
        dto.setEventId(review.getEvent().getId());
        dto.setEventName(review.getEvent().getName());

        if (review.getRate() != null) {
            RateDTO rateDTO = new RateDTO();
            rateDTO.setId(review.getRate().getId());
            rateDTO.setPerformance(review.getRate().getPerformance());
            rateDTO.setSoundAndLighting(review.getRate().getSoundAndLighting());
            rateDTO.setVenue(review.getRate().getVenue());
            rateDTO.setOverallImpression(review.getRate().getOverallImpression());
            rateDTO.setAverageRating(review.getRate().getAverageRating());
            dto.setRate(rateDTO);
        }

        return dto;
    }

    private ManagedLocationDTO mapToManagedLocationDTO(Manages manages) {
        ManagedLocationDTO dto = new ManagedLocationDTO();
        dto.setId(manages.getId());
        dto.setLocationName(manages.getLocation().getName());
        dto.setLocationAddress(manages.getLocation().getAddress());
        dto.setLocationType(manages.getLocation().getType());
        dto.setStartDate(manages.getStartDate());
        dto.setEndDate(manages.getEndDate());
        dto.setIsActive(manages.isActive());
        return dto;
    }

    private void deleteAvatarFile(String filename) {
        try {
            Path filePath = Paths.get(uploadDir).resolve(filename);
            Files.deleteIfExists(filePath);
            log.info("Old avatar file deleted: {}", filename);
        } catch (IOException e) {
            log.error("Failed to delete old avatar file: {}", filename, e);
        }
    }
}
