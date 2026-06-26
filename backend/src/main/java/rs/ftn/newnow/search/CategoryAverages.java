package rs.ftn.newnow.search;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import rs.ftn.newnow.model.Rate;
import rs.ftn.newnow.model.Review;

import java.util.List;
import java.util.function.Function;

/**
 * Per-category averages for a place, computed over the rating-source set
 * (deleted=false AND deletedByManager=false; hidden still counts — per M2).
 *
 * Each field averages ONLY over reviews where that specific category was rated
 * (the underlying Rate column is nullable). If no review rated a category, the
 * field stays null, which means ES leaves that field absent in the document
 * and a range query on it correctly returns nothing for that place.
 *
 * {@code avgOverallImpression} is read directly from {@code Rate.overallImpression};
 * it is its own rated category, NOT the mean of the other three.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryAverages {

    private Double avgPerformance;
    private Double avgSoundAndLighting;
    private Double avgVenue;
    private Double avgOverallImpression;

    public static CategoryAverages compute(List<Review> ratingSource) {
        return new CategoryAverages(
                averageOf(ratingSource, Rate::getPerformance),
                averageOf(ratingSource, Rate::getSoundAndLighting),
                averageOf(ratingSource, Rate::getVenue),
                averageOf(ratingSource, Rate::getOverallImpression)
        );
    }

    private static Double averageOf(List<Review> reviews, Function<Rate, Integer> extractor) {
        long count = 0;
        long sum = 0;
        for (Review r : reviews) {
            Rate rate = r.getRate();
            if (rate == null) continue;
            Integer v = extractor.apply(rate);
            if (v == null) continue;
            sum += v;
            count++;
        }
        return count == 0 ? null : (double) sum / count;
    }
}
