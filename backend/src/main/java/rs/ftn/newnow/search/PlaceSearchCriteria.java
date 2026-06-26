package rs.ftn.newnow.search;

import lombok.Builder;
import lombok.Value;

/**
 * Immutable filter set for the place search endpoint. Each bound is independently
 * optional. Two bounds set on the same field are validated for from <= to at the
 * controller boundary so the query builder can assume sanity.
 */
@Value
@Builder
public class PlaceSearchCriteria {
    String name;
    String description;
    String pdf;
    Integer reviewsFrom;
    Integer reviewsTo;
    Double avgPerformanceFrom;
    Double avgPerformanceTo;
    Double avgSoundAndLightingFrom;
    Double avgSoundAndLightingTo;
    Double avgVenueFrom;
    Double avgVenueTo;
    Double avgOverallImpressionFrom;
    Double avgOverallImpressionTo;
    int page;
    int size;
}
