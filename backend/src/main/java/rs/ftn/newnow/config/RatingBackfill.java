package rs.ftn.newnow.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import rs.ftn.newnow.model.Location;
import rs.ftn.newnow.model.Review;
import rs.ftn.newnow.repository.LocationRepository;
import rs.ftn.newnow.repository.ReviewRepository;

import java.util.List;

@Component
@Order(10)
@RequiredArgsConstructor
@Slf4j
public class RatingBackfill implements CommandLineRunner {

    private final LocationRepository locationRepository;
    private final ReviewRepository reviewRepository;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("=== RatingBackfill: recomputing Location.totalRating with M2-correct rules");
        int updated = 0;
        for (Location location : locationRepository.findAll()) {
            List<Review> reviews = reviewRepository.findRatingSourceForLocation(location.getId());
            double avg = reviews.stream()
                    .filter(r -> r.getRate() != null)
                    .mapToDouble(r -> r.getRate().getAverageRating())
                    .average()
                    .orElse(0.0);
            if (location.getTotalRating() == null || Math.abs(location.getTotalRating() - avg) > 1e-9) {
                location.setTotalRating(avg);
                locationRepository.save(location);
                updated++;
            }
        }
        log.info("=== RatingBackfill: updated {} locations", updated);
    }
}
