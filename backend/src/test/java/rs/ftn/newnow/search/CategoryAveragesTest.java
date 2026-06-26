package rs.ftn.newnow.search;

import org.junit.jupiter.api.Test;
import rs.ftn.newnow.model.Rate;
import rs.ftn.newnow.model.Review;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Pure-Java tests for the per-category averaging rule.
 *
 * The actual data fetch (findRatingSourceForLocation) is exercised elsewhere; here we
 * only test the math, because the math is the part that the audit said could drift.
 *
 * Rules tested:
 *  - hidden reviews are included
 *  - removed reviews (deleted OR deletedByManager) are excluded
 *  - each category averages only over its own non-null values
 *  - overall is read directly, not derived from the other three
 *  - a category with zero non-null values yields null
 */
class CategoryAveragesTest {

    private static Review review(boolean hidden, boolean deleted, boolean deletedByManager,
                                 Integer perf, Integer sound, Integer venue, Integer overall) {
        Review r = new Review();
        r.setHidden(hidden);
        r.setDeleted(deleted);
        r.setDeletedByManager(deletedByManager);
        Rate rate = new Rate();
        rate.setPerformance(perf);
        rate.setSoundAndLighting(sound);
        rate.setVenue(venue);
        rate.setOverallImpression(overall);
        r.setRate(rate);
        return r;
    }

    /** Mirror the rule applied by {@code findRatingSourceForLocation}: deleted=false AND deletedByManager=false (hidden left in). */
    private static List<Review> ratingSource(List<Review> all) {
        List<Review> kept = new ArrayList<>();
        for (Review r : all) {
            if (Boolean.TRUE.equals(r.getDeleted())) continue;
            if (Boolean.TRUE.equals(r.getDeletedByManager())) continue;
            kept.add(r);
        }
        return kept;
    }

    @Test
    void averagesIgnoreRemovedReviewsAndKeepHiddenOnes() {
        List<Review> source = ratingSource(List.of(
                review(false, false, false, 8, 6, 7, 9),   // counts
                review(true,  false, false, 4, 4, 4, 4),   // hidden — counts
                review(false, true,  false, 1, 1, 1, 1),   // user-deleted — excluded
                review(false, false, true,  2, 2, 2, 2)    // manager-removed — excluded
        ));
        CategoryAverages avgs = CategoryAverages.compute(source);
        assertEquals(6.0, avgs.getAvgPerformance(), 1e-9);
        assertEquals(5.0, avgs.getAvgSoundAndLighting(), 1e-9);
        assertEquals(5.5, avgs.getAvgVenue(), 1e-9);
        assertEquals(6.5, avgs.getAvgOverallImpression(), 1e-9);
    }

    @Test
    void perCategoryAverageIsTakenOnlyOverItsOwnNonNullValues() {
        List<Review> source = ratingSource(List.of(
                review(false, false, false, 10, null, 5, 7),
                review(false, false, false, null, 8, null, 9)
        ));
        CategoryAverages avgs = CategoryAverages.compute(source);
        // performance: only one non-null (10)
        assertEquals(10.0, avgs.getAvgPerformance(), 1e-9);
        // sound: only one non-null (8)
        assertEquals(8.0, avgs.getAvgSoundAndLighting(), 1e-9);
        // venue: only one non-null (5)
        assertEquals(5.0, avgs.getAvgVenue(), 1e-9);
        // overall: both non-null
        assertEquals(8.0, avgs.getAvgOverallImpression(), 1e-9);
    }

    @Test
    void categoryWithAllNullValuesReturnsNullSoEsLeavesFieldMissing() {
        List<Review> source = ratingSource(List.of(
                review(false, false, false, 7, null, 7, 7),
                review(false, false, false, 8, null, 8, 8)
        ));
        CategoryAverages avgs = CategoryAverages.compute(source);
        assertNull(avgs.getAvgSoundAndLighting(),
                "no review rated this category → null → ES leaves the field missing → range-filtered out");
        assertEquals(7.5, avgs.getAvgPerformance(), 1e-9);
    }

    @Test
    void overallIsReadDirectlyNotDerivedFromOtherCategories() {
        List<Review> source = ratingSource(List.of(
                // overall = 3 even though the other three are high — must not be averaged together
                review(false, false, false, 10, 10, 10, 3)
        ));
        CategoryAverages avgs = CategoryAverages.compute(source);
        assertEquals(3.0, avgs.getAvgOverallImpression(), 1e-9);
    }

    @Test
    void emptySourceReturnsAllNulls() {
        CategoryAverages avgs = CategoryAverages.compute(List.of());
        assertNull(avgs.getAvgPerformance());
        assertNull(avgs.getAvgSoundAndLighting());
        assertNull(avgs.getAvgVenue());
        assertNull(avgs.getAvgOverallImpression());
    }

    @Test
    void reviewWithoutAnyRateInstanceIsTreatedAsAllNulls() {
        Review r = new Review();
        r.setHidden(false);
        r.setDeleted(false);
        r.setDeletedByManager(false);
        r.setRate(null);
        CategoryAverages avgs = CategoryAverages.compute(List.of(r));
        assertNull(avgs.getAvgPerformance());
        assertNull(avgs.getAvgSoundAndLighting());
        assertNull(avgs.getAvgVenue());
        assertNull(avgs.getAvgOverallImpression());
    }
}
