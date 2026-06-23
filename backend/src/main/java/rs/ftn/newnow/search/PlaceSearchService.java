package rs.ftn.newnow.search;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;
import rs.ftn.newnow.search.dto.LocationSearchResultDTO;
import rs.ftn.newnow.search.dto.PlaceSearchPageResponse;
import rs.ftn.newnow.search.index.LocationIndex;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlaceSearchService {

    private final ElasticsearchOperations elasticsearchOperations;

    public PlaceSearchPageResponse search(PlaceSearchCriteria c) {
        Query query = buildQuery(c);

        NativeQuery nq = NativeQuery.builder()
                .withQuery(query)
                .withPageable(PageRequest.of(c.getPage(), c.getSize()))
                .build();

        SearchHits<LocationIndex> hits = elasticsearchOperations.search(nq, LocationIndex.class);
        List<LocationSearchResultDTO> content = hits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .map(this::toResult)
                .collect(Collectors.toList());

        long total = hits.getTotalHits();
        int totalPages = c.getSize() > 0 ? (int) Math.ceil((double) total / c.getSize()) : 0;

        return PlaceSearchPageResponse.builder()
                .content(content)
                .totalElements(total)
                .totalPages(totalPages)
                .page(c.getPage())
                .size(c.getSize())
                .build();
    }

    /** Visible for tests — the query builder is what we care about most. */
    Query buildQuery(PlaceSearchCriteria c) {
        BoolQuery.Builder bool = new BoolQuery.Builder();
        // Always exclude soft-deleted places.
        bool.filter(Query.of(q -> q.term(t -> t.field("deleted").value(false))));

        boolean any = false;

        if (c.getName() != null && !c.getName().isBlank()) {
            bool.must(Query.of(q -> q.match(m -> m.field("name").query(c.getName().trim()))));
            any = true;
        }
        if (c.getDescription() != null && !c.getDescription().isBlank()) {
            bool.must(Query.of(q -> q.match(m -> m.field("description").query(c.getDescription().trim()))));
            any = true;
        }
        if (c.getPdf() != null && !c.getPdf().isBlank()) {
            bool.must(Query.of(q -> q.match(m -> m.field("pdfDescription").query(c.getPdf().trim()))));
            any = true;
        }

        if (addIntRange(bool, "reviewCount", c.getReviewsFrom(), c.getReviewsTo())) any = true;
        if (addDoubleRange(bool, "avgPerformance", c.getAvgPerformanceFrom(), c.getAvgPerformanceTo())) any = true;
        if (addDoubleRange(bool, "avgSoundAndLighting", c.getAvgSoundAndLightingFrom(), c.getAvgSoundAndLightingTo())) any = true;
        if (addDoubleRange(bool, "avgVenue", c.getAvgVenueFrom(), c.getAvgVenueTo())) any = true;
        if (addDoubleRange(bool, "avgOverallImpression", c.getAvgOverallImpressionFrom(), c.getAvgOverallImpressionTo())) any = true;

        if (!any) {
            bool.must(Query.of(q -> q.matchAll(m -> m)));
        }

        return Query.of(q -> q.bool(bool.build()));
    }

    private static boolean addIntRange(BoolQuery.Builder bool, String field, Integer from, Integer to) {
        if (from == null && to == null) return false;
        bool.must(Query.of(q -> q.range(r -> r.number(n -> {
            n.field(field);
            if (from != null) n.gte(from.doubleValue());
            if (to != null) n.lte(to.doubleValue());
            return n;
        }))));
        return true;
    }

    private static boolean addDoubleRange(BoolQuery.Builder bool, String field, Double from, Double to) {
        if (from == null && to == null) return false;
        bool.must(Query.of(q -> q.range(r -> r.number(n -> {
            n.field(field);
            if (from != null) n.gte(from);
            if (to != null) n.lte(to);
            return n;
        }))));
        return true;
    }

    private LocationSearchResultDTO toResult(LocationIndex idx) {
        return LocationSearchResultDTO.builder()
                .id(Long.parseLong(idx.getId()))
                .name(idx.getName())
                .description(idx.getDescription())
                .address(idx.getAddress())
                .type(idx.getType())
                .reviewCount(idx.getReviewCount())
                .totalRating(idx.getTotalRating())
                .avgPerformance(idx.getAvgPerformance())
                .avgSoundAndLighting(idx.getAvgSoundAndLighting())
                .avgVenue(idx.getAvgVenue())
                .avgOverallImpression(idx.getAvgOverallImpression())
                .imageUrl(idx.getImageUrl())
                .hasPdf(idx.getPdfObjectKey() != null)
                .build();
    }
}
