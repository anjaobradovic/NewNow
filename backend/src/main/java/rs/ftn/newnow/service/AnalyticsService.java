package rs.ftn.newnow.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rs.ftn.newnow.dto.*;
import rs.ftn.newnow.model.Location;
import rs.ftn.newnow.model.User;
import rs.ftn.newnow.model.enums.Role;
import rs.ftn.newnow.repository.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AnalyticsService {

    private final LocationRepository locationRepository;
    private final EventRepository eventRepository;
    private final ReviewRepository reviewRepository;
    private final ManagesRepository managesRepository;

    public boolean isManagerOfLocation(Long locationId, String email) {
        locationRepository.findByIdAndDeletedFalse(locationId)
                .orElseThrow(() -> new IllegalArgumentException("Location not found"));

        return managesRepository.findActiveManagersByLocation(locationId, LocalDate.now())
                .stream()
                .anyMatch(manages -> manages.getUser().getEmail().equals(email));
    }

    public LocationSummaryDTO getLocationSummary(Long locationId, String period, LocalDate startDate, LocalDate endDate, User currentUser) {
        validateAccess(locationId, currentUser);

        Location location = locationRepository.findByIdAndDeletedFalse(locationId)
                .orElseThrow(() -> new IllegalArgumentException("Location not found"));

        DateRange dateRange = calculateDateRange(period, startDate, endDate);

        Long totalEvents = eventRepository.countByLocationAndDateRange(
                locationId, dateRange.getStartDate(), dateRange.getEndDate());
        Long totalReviews = reviewRepository.countByLocationAndDateRange(
                locationId, dateRange.getStartDate().atStartOfDay(), dateRange.getEndDate().atTime(23, 59, 59));
        Double averageRating = reviewRepository.calculateAverageRatingByLocationAndDateRange(
                locationId, dateRange.getStartDate().atStartOfDay(), dateRange.getEndDate().atTime(23, 59, 59));
        Long totalVisitors = reviewRepository.countDistinctUsersByLocationAndDateRange(
                locationId, dateRange.getStartDate().atStartOfDay(), dateRange.getEndDate().atTime(23, 59, 59));

        LocationSummaryDTO summary = new LocationSummaryDTO();
        summary.setLocationId(locationId);
        summary.setLocationName(location.getName());
        summary.setPeriod(period);
        summary.setStartDate(dateRange.getStartDate());
        summary.setEndDate(dateRange.getEndDate());
        summary.setTotalEvents(totalEvents != null ? totalEvents : 0L);
        summary.setTotalReviews(totalReviews != null ? totalReviews : 0L);
        summary.setAverageRating(averageRating != null ? averageRating : 0.0);
        summary.setTotalVisitors(totalVisitors != null ? totalVisitors : 0L);

        return summary;
    }

    public EventCountsDTO getLocationEventCounts(Long locationId, User currentUser) {
        validateAccess(locationId, currentUser);

        locationRepository.findByIdAndDeletedFalse(locationId)
                .orElseThrow(() -> new IllegalArgumentException("Location not found"));

        Long totalEvents = eventRepository.countByLocation(locationId);
        Long regularEvents = eventRepository.countByLocationAndRecurrent(locationId, true);
        Long nonRegularEvents = eventRepository.countByLocationAndRecurrent(locationId, false);
        Long freeEvents = eventRepository.countByLocationAndPrice(locationId, 0.0);
        Long paidEvents = eventRepository.countByLocationAndPriceGreaterThan(locationId, 0.0);

        EventCountsDTO counts = new EventCountsDTO();
        counts.setTotalEvents(totalEvents != null ? totalEvents : 0L);
        counts.setRegularEvents(regularEvents != null ? regularEvents : 0L);
        counts.setNonRegularEvents(nonRegularEvents != null ? nonRegularEvents : 0L);
        counts.setFreeEvents(freeEvents != null ? freeEvents : 0L);
        counts.setPaidEvents(paidEvents != null ? paidEvents : 0L);

        return counts;
    }

    public TopRatingsDTO getTopRatings(Long locationId, int limit, String direction, User currentUser) {
        validateAccess(locationId, currentUser);

        Location location = locationRepository.findByIdAndDeletedFalse(locationId)
                .orElseThrow(() -> new IllegalArgumentException("Location not found"));

        boolean ascending = "asc".equalsIgnoreCase(direction);
        Pageable pageable = PageRequest.of(0, limit);

        List<Object[]> eventRatings = ascending ?
                reviewRepository.findEventRatingsAscByLocation(locationId, pageable) :
                reviewRepository.findEventRatingsDescByLocation(locationId, pageable);

        List<EventRatingDTO> topEvents = eventRatings.stream()
                .map(row -> new EventRatingDTO(
                        ((Number) row[0]).longValue(),
                        (String) row[1],
                        ((Number) row[2]).doubleValue(),
                        ((Number) row[3]).longValue()
                ))
                .collect(Collectors.toList());

        Long reviewCount = reviewRepository.countByLocationAndNotDeleted(locationId);
        Double averageRating = reviewRepository.calculateAverageRatingByLocation(locationId);

        LocationRatingDTO locationRating = new LocationRatingDTO();
        locationRating.setLocationId(locationId);
        locationRating.setLocationName(location.getName());
        locationRating.setAverageRating(averageRating != null ? averageRating : 0.0);
        locationRating.setReviewCount(reviewCount != null ? reviewCount : 0L);

        TopRatingsDTO topRatings = new TopRatingsDTO();
        topRatings.setTopEvents(topEvents);
        topRatings.setLocationRating(locationRating);

        return topRatings;
    }

    public List<ReviewDTO> getLatestReviews(Long locationId, User currentUser) {
        validateAccess(locationId, currentUser);

        locationRepository.findByIdAndDeletedFalse(locationId)
                .orElseThrow(() -> new IllegalArgumentException("Location not found"));

        Long mostPopularLocationId = reviewRepository.findMostPopularLocationInPeriod(
                LocalDate.now().minusMonths(1).atStartOfDay(), 
                LocalDate.now().atTime(23, 59, 59));

        if (mostPopularLocationId == null) {
            mostPopularLocationId = locationId;
        }

        Pageable pageable = PageRequest.of(0, 3);
        return reviewRepository.findTop3ByLocationId(mostPopularLocationId, pageable)
                .stream()
                .map(this::convertToReviewDTO)
                .collect(Collectors.toList());
    }

    private void validateAccess(Long locationId, User currentUser) {
        boolean isAdmin = currentUser.getRoles().contains(Role.ROLE_ADMIN);
        boolean isManager = managesRepository.findActiveManagersByLocation(locationId, LocalDate.now())
                .stream()
                .anyMatch(manages -> manages.getUser().getId().equals(currentUser.getId()));

        if (!isAdmin && !isManager) {
            throw new IllegalArgumentException("User does not have permission to access this location's analytics");
        }
    }

    private DateRange calculateDateRange(String period, LocalDate startDate, LocalDate endDate) {
        if ("custom".equalsIgnoreCase(period)) {
            if (startDate == null || endDate == null) {
                throw new IllegalArgumentException("Start date and end date are required for custom period");
            }
            return new DateRange(startDate, endDate);
        }

        LocalDate end = LocalDate.now();
        LocalDate start;

        switch (period.toLowerCase()) {
            case "weekly":
                start = end.minusWeeks(1);
                break;
            case "monthly":
                start = end.minusMonths(1);
                break;
            case "yearly":
                start = end.minusYears(1);
                break;
            default:
                start = end.minusMonths(1);
        }

        return new DateRange(start, end);
    }

    private ReviewDTO convertToReviewDTO(rs.ftn.newnow.model.Review review) {
        ReviewDTO dto = new ReviewDTO();
        dto.setId(review.getId());
        dto.setCreatedAt(review.getCreatedAt());
        dto.setEventCount(review.getEventCount());
        dto.setHidden(review.getHidden());

        if (review.getEvent() != null) {
            dto.setEventId(review.getEvent().getId());
            dto.setEventName(review.getEvent().getName());
        }

        if (review.getLocation() != null) {
            dto.setLocationId(review.getLocation().getId());
            dto.setLocationName(review.getLocation().getName());
        }

        if (review.getRate() != null) {
            RateDTO rateDTO = new RateDTO();
            rateDTO.setId(review.getRate().getId());
            rateDTO.setPerformance(review.getRate().getPerformance());
            rateDTO.setSoundAndLighting(review.getRate().getSoundLight());
            rateDTO.setVenue(review.getRate().getSpace());
            rateDTO.setOverallImpression(review.getRate().getOverall());
            rateDTO.setAverageRating(review.getRate().getAverageRating());
            dto.setRate(rateDTO);
        }

        return dto;
    }

    private static class DateRange {
        private final LocalDate startDate;
        private final LocalDate endDate;

        public DateRange(LocalDate startDate, LocalDate endDate) {
            this.startDate = startDate;
            this.endDate = endDate;
        }

        public LocalDate getStartDate() {
            return startDate;
        }

        public LocalDate getEndDate() {
            return endDate;
        }
    }
}
