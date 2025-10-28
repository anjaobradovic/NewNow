package rs.ftn.newnow.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.ftn.newnow.dto.*;
import rs.ftn.newnow.model.*;
import rs.ftn.newnow.repository.*;

import rs.ftn.newnow.exception.BusinessException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final EventRepository eventRepository;
    private final LocationRepository locationRepository;
    private final UserRepository userRepository;
    private final ManagesRepository managesRepository;
    private static final int EDIT_DEADLINE_HOURS = 24;

    @Transactional
    public ReviewDetailsDTO createReview(Long locationId, CreateReviewDTO dto, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessException("User not found"));

        Location location = locationRepository.findByIdAndDeletedFalse(locationId)
                .orElseThrow(() -> new BusinessException("Location not found"));

        Event event = eventRepository.findByIdAndNotDeleted(dto.getEventId())
                .orElseThrow(() -> new BusinessException("Event not found"));

        if (!event.getLocation().getId().equals(locationId)) {
            throw new BusinessException("Event does not belong to this location");
        }

        if (!event.getRecurrent()) {
            throw new BusinessException("Only recurrent (regular) events can be reviewed");
        }

        // Check if event is active (not in the future) - must be today or in the past
        if (event.getDate().isAfter(LocalDate.now())) {
            throw new BusinessException("Cannot review future events. Event must be active (today) or past.");
        }

        reviewRepository.findByEventIdAndUserId(event.getId(), user.getId())
                .ifPresent(r -> {
                    throw new BusinessException("You have already reviewed this event");
                });

        Long eventCount = eventRepository.countOccurrencesUntilDate(event.getId(), LocalDate.now());

        Review review = new Review();
        review.setUser(user);
        review.setLocation(location);
        review.setEvent(event);
        review.setComment(dto.getComment());
        review.setEventCount(eventCount.intValue());
        review.setHidden(false);
        review.setDeleted(false);
        review.setDeletedByManager(false);
        review.setCreatedAt(LocalDateTime.now());

        Rate rate = new Rate();
        rate.setPerformance(dto.getPerformance());
        rate.setSoundAndLighting(dto.getSoundAndLighting());
        rate.setVenue(dto.getVenue());
        rate.setOverallImpression(dto.getOverallImpression());
        rate.setReview(review);
        review.setRate(rate);

        review = reviewRepository.save(review);
        updateLocationRating(location);

        return mapToDetailsDTO(review);
    }

    @Transactional(readOnly = true)
    public ReviewDetailsDTO getReviewDetails(Long reviewId) {
        Review review = reviewRepository.findByIdAndNotDeleted(reviewId)
                .orElseThrow(() -> new BusinessException("Review not found"));
        return mapToDetailsDTO(review);
    }

    @Transactional
    public ReviewDetailsDTO updateReview(Long reviewId, UpdateReviewDTO dto, String userEmail) {
        Review review = reviewRepository.findByIdAndNotDeleted(reviewId)
                .orElseThrow(() -> new BusinessException("Review not found"));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessException("User not found"));

        if (!review.getUser().getId().equals(user.getId())) {
            throw new BusinessException("You can only edit your own reviews");
        }

        LocalDateTime deadline = review.getCreatedAt().plusHours(EDIT_DEADLINE_HOURS);
        if (LocalDateTime.now().isAfter(deadline)) {
            throw new BusinessException("Review edit deadline has passed");
        }

        Rate rate = review.getRate();
        rate.setPerformance(dto.getPerformance());
        rate.setSoundAndLighting(dto.getSoundAndLighting());
        rate.setVenue(dto.getVenue());
        rate.setOverallImpression(dto.getOverallImpression());
        review.setComment(dto.getComment());

        review = reviewRepository.save(review);
        updateLocationRating(review.getLocation());

        return mapToDetailsDTO(review);
    }

    @Transactional
    public void deleteReview(Long reviewId, String userEmail) {
        Review review = reviewRepository.findByIdAndNotDeleted(reviewId)
                .orElseThrow(() -> new BusinessException("Review not found"));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessException("User not found"));

        if (!review.getUser().getId().equals(user.getId())) {
            throw new BusinessException("You can only delete your own reviews");
        }

        review.setDeleted(true);
        reviewRepository.save(review);
        updateLocationRating(review.getLocation());
    }

    @Transactional(readOnly = true)
    public Page<ReviewDetailsDTO> getLocationReviews(Long locationId, String sort, String order, int page, int size) {
        locationRepository.findByIdAndDeletedFalse(locationId)
                .orElseThrow(() -> new BusinessException("Location not found"));

        Pageable pageable = PageRequest.of(page, size);
        Page<Review> reviews;

        boolean ascending = "asc".equalsIgnoreCase(order);

        if ("rating".equalsIgnoreCase(sort)) {
            reviews = ascending ? 
                    reviewRepository.findByLocationIdOrderByRatingAsc(locationId, pageable) :
                    reviewRepository.findByLocationIdOrderByRating(locationId, pageable);
        } else {
            reviews = ascending ? 
                    reviewRepository.findByLocationIdOrderByDateAsc(locationId, pageable) :
                    reviewRepository.findByLocationIdOrderByDate(locationId, pageable);
        }

        // Filter out hidden reviews for public view
        return reviews.map(this::mapToDetailsDTO);
    }

    @Transactional(readOnly = true)
    public Page<ReviewDetailsDTO> getLocationReviewsForManager(Long locationId, String sort, String order, int page, int size) {
        locationRepository.findByIdAndDeletedFalse(locationId)
                .orElseThrow(() -> new BusinessException("Location not found"));

        Pageable pageable = PageRequest.of(page, size);
        Page<Review> reviews;

        boolean ascending = "asc".equalsIgnoreCase(order);

        // Manager sees ALL reviews including hidden ones (but not deleted)
        if ("rating".equalsIgnoreCase(sort)) {
            reviews = ascending ? 
                    reviewRepository.findByLocationIdIncludingHiddenOrderByRatingAsc(locationId, pageable) :
                    reviewRepository.findByLocationIdIncludingHiddenOrderByRating(locationId, pageable);
        } else {
            reviews = ascending ? 
                    reviewRepository.findByLocationIdIncludingHiddenOrderByDateAsc(locationId, pageable) :
                    reviewRepository.findByLocationIdIncludingHiddenOrderByDate(locationId, pageable);
        }

        return reviews.map(this::mapToDetailsDTO);
    }

    @Transactional
    public void hideReview(Long reviewId, boolean hidden, String userEmail) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new BusinessException("Review not found"));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessException("User not found"));

        validateManagerAccess(user.getId(), review.getLocation().getId());

        review.setHidden(hidden);
        reviewRepository.save(review);
    }

    @Transactional
    public void deleteReviewByManager(Long reviewId, String userEmail) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new BusinessException("Review not found"));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessException("User not found"));

        validateManagerAccess(user.getId(), review.getLocation().getId());

        review.setDeletedByManager(true);
        reviewRepository.save(review);
        updateLocationRating(review.getLocation());
    }

    @Transactional(readOnly = true)
    public List<ReviewDetailsDTO> getLatestReviewsForLocation(Long locationId, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<Review> reviews = reviewRepository.findTop3ByLocationId(locationId, pageable);
        return reviews.stream()
                .map(this::mapToDetailsDTO)
                .collect(Collectors.toList());
    }

    private void validateManagerAccess(Long userId, Long locationId) {
        List<Manages> activeManages = managesRepository.findActiveManagement(
                userId, locationId, LocalDate.now());
        
        if (activeManages.isEmpty()) {
            throw new BusinessException("You are not a manager of this location");
        }
    }

    private void updateLocationRating(Location location) {
        List<Review> reviews = reviewRepository.findByLocationIdAndNotDeleted(location.getId(), Pageable.unpaged()).getContent();
        
        double averageRating = reviews.stream()
                .filter(r -> !r.getDeletedByManager())
                .mapToDouble(r -> r.getRate().getAverageRating())
                .average()
                .orElse(0.0);
        
        location.setTotalRating(averageRating);
        locationRepository.save(location);
    }

    private ReviewDetailsDTO mapToDetailsDTO(Review review) {
        ReviewDetailsDTO dto = new ReviewDetailsDTO();
        dto.setId(review.getId());
        dto.setCreatedAt(review.getCreatedAt());
        dto.setComment(review.getComment());
        dto.setEventCount(review.getEventCount());
        dto.setHidden(review.getHidden());

        UserBasicDTO authorDTO = new UserBasicDTO();
        authorDTO.setId(review.getUser().getId());
        authorDTO.setName(review.getUser().getName());
        authorDTO.setEmail(review.getUser().getEmail());
        dto.setAuthor(authorDTO);

        EventBasicDTO eventDTO = new EventBasicDTO();
        eventDTO.setId(review.getEvent().getId());
        eventDTO.setName(review.getEvent().getName());
        eventDTO.setType(review.getEvent().getType());
        eventDTO.setDate(review.getEvent().getDate());
        eventDTO.setRecurrent(review.getEvent().getRecurrent());
        dto.setEvent(eventDTO);

        Rate rate = review.getRate();
        RateDetailsDTO rateDTO = new RateDetailsDTO();
        rateDTO.setPerformance(rate.getPerformance());
        rateDTO.setSoundAndLighting(rate.getSoundAndLighting());
        rateDTO.setVenue(rate.getVenue());
        rateDTO.setOverallImpression(rate.getOverallImpression());
        rateDTO.setAverage(rate.getAverageRating());
        dto.setRatings(rateDTO);

        return dto;
    }
}
