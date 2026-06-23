package rs.ftn.newnow.search;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the ES query shape — we only inspect the typed {@link Query} the builder
 * produces, not a live ES instance. The shape is the contract; the live behaviour is
 * verified by the smoke tests against the actual cluster.
 */
class PlaceSearchServiceTest {

    private final PlaceSearchService service = new PlaceSearchService(null);

    @Test
    void noFiltersProducesMatchAll() {
        Query q = service.buildQuery(PlaceSearchCriteria.builder().page(0).size(10).build());
        assertNotNull(q.bool());
        // A single matchAll must clause keeps the result set unconstrained beyond deleted=false.
        boolean hasMatchAll = q.bool().must().stream().anyMatch(Query::isMatchAll);
        assertTrue(hasMatchAll, "expected a match_all clause when no filters are supplied");
    }

    @Test
    void onlyLowerBoundOnPerformance() {
        Query q = service.buildQuery(PlaceSearchCriteria.builder()
                .avgPerformanceFrom(8.0).page(0).size(10).build());
        var range = firstRangeOn(q, "avgPerformance");
        assertNotNull(range, "expected a range clause for avgPerformance");
        var number = range.number();
        assertEquals(8.0, number.gte());
        assertNull(number.lte(), "lte must be absent when only the lower bound is supplied");
    }

    @Test
    void onlyUpperBoundOnVenue() {
        Query q = service.buildQuery(PlaceSearchCriteria.builder()
                .avgVenueTo(6.5).page(0).size(10).build());
        var range = firstRangeOn(q, "avgVenue");
        assertNotNull(range);
        var number = range.number();
        assertNull(number.gte(), "gte must be absent when only the upper bound is supplied");
        assertEquals(6.5, number.lte());
    }

    @Test
    void bothBoundsOnSoundAndLighting() {
        Query q = service.buildQuery(PlaceSearchCriteria.builder()
                .avgSoundAndLightingFrom(7.0)
                .avgSoundAndLightingTo(9.0)
                .page(0).size(10).build());
        var range = firstRangeOn(q, "avgSoundAndLighting");
        assertNotNull(range);
        assertEquals(7.0, range.number().gte());
        assertEquals(9.0, range.number().lte());
    }

    @Test
    void neitherBoundOnOverallProducesNoRangeClauseForThatField() {
        Query q = service.buildQuery(PlaceSearchCriteria.builder()
                .name("arena")
                .page(0).size(10).build());
        assertNull(firstRangeOn(q, "avgOverallImpression"),
                "no bounds → no range clause for that category");
    }

    @Test
    void allFourCategoryRangesCanCoexist() {
        Query q = service.buildQuery(PlaceSearchCriteria.builder()
                .avgPerformanceFrom(7.0)
                .avgSoundAndLightingFrom(7.0)
                .avgVenueTo(9.0)
                .avgOverallImpressionFrom(6.0)
                .avgOverallImpressionTo(10.0)
                .page(0).size(10).build());

        assertNotNull(firstRangeOn(q, "avgPerformance"));
        assertNotNull(firstRangeOn(q, "avgSoundAndLighting"));
        assertNotNull(firstRangeOn(q, "avgVenue"));
        assertNotNull(firstRangeOn(q, "avgOverallImpression"));
    }

    private static co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery firstRangeOn(Query q, String field) {
        return q.bool().must().stream()
                .filter(Query::isRange)
                .map(Query::range)
                .filter(r -> field.equals(r.number().field()))
                .findFirst()
                .orElse(null);
    }
}
