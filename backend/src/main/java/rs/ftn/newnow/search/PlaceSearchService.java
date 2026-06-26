package rs.ftn.newnow.search;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${newnow.search.fuzzy-distance:AUTO}")
    private String fuzzyDistance = "AUTO";

    @Value("${newnow.search.mlt.min-term-freq:1}")
    private int mltMinTermFreq = 1;

    @Value("${newnow.search.mlt.min-doc-freq:1}")
    private int mltMinDocFreq = 1;

    @Value("${newnow.search.mlt.max-query-terms:25}")
    private int mltMaxQueryTerms = 25;

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
        // Always exclude soft-deleted places, regardless of operator.
        bool.filter(Query.of(q -> q.term(t -> t.field("deleted").value(false))));

        java.util.List<Query> clauses = new java.util.ArrayList<>();

        addTextClause(clauses, "name",           c.getName());
        addTextClause(clauses, "description",    c.getDescription());
        addTextClause(clauses, "pdfDescription", c.getPdf());
        addIntRange(clauses, "reviewCount", c.getReviewsFrom(), c.getReviewsTo());
        addDoubleRange(clauses, "avgPerformance",       c.getAvgPerformanceFrom(),       c.getAvgPerformanceTo());
        addDoubleRange(clauses, "avgSoundAndLighting",  c.getAvgSoundAndLightingFrom(),  c.getAvgSoundAndLightingTo());
        addDoubleRange(clauses, "avgVenue",             c.getAvgVenueFrom(),             c.getAvgVenueTo());
        addDoubleRange(clauses, "avgOverallImpression", c.getAvgOverallImpressionFrom(), c.getAvgOverallImpressionTo());

        if (clauses.isEmpty()) {
            // No user filters → return all non-deleted places.
            bool.must(Query.of(q -> q.matchAll(m -> m)));
        } else if (c.getOperator() == BoolOperator.OR) {
            clauses.forEach(bool::should);
            bool.minimumShouldMatch("1");
        } else {
            clauses.forEach(bool::must);
        }

        return Query.of(q -> q.bool(bool.build()));
    }

    /**
     * Inspects the raw user input and dispatches to the right ES query type:
     * "phrase" → match_phrase, contains * → match_bool_prefix (analyzer-aware),
     * leading ~ → match with fuzziness, plain text → match. Empty input is skipped.
     */
    private void addTextClause(java.util.List<Query> sink, String field, String raw) {
        SearchSyntax.Parsed p = SearchSyntax.parse(raw);
        switch (p.mode()) {
            case EMPTY -> {}
            case PHRASE -> sink.add(Query.of(q -> q.matchPhrase(m -> m.field(field).query(p.term()))));
            case PREFIX -> sink.add(Query.of(q -> q.matchBoolPrefix(m -> m.field(field).query(p.term()))));
            case FUZZY  -> sink.add(Query.of(q -> q.match(m -> m.field(field).query(p.term()).fuzziness(fuzzyDistance))));
            case MATCH  -> sink.add(Query.of(q -> q.match(m -> m.field(field).query(p.term()))));
        }
    }

    private static void addIntRange(java.util.List<Query> sink, String field, Integer from, Integer to) {
        if (from == null && to == null) return;
        sink.add(Query.of(q -> q.range(r -> r.number(n -> {
            n.field(field);
            if (from != null) n.gte(from.doubleValue());
            if (to != null) n.lte(to.doubleValue());
            return n;
        }))));
    }

    private static void addDoubleRange(java.util.List<Query> sink, String field, Double from, Double to) {
        if (from == null && to == null) return;
        sink.add(Query.of(q -> q.range(r -> r.number(n -> {
            n.field(field);
            if (from != null) n.gte(from);
            if (to != null) n.lte(to);
            return n;
        }))));
    }

    /**
     * "Find similar places" — runs an ES more_like_this over name + description + PDF text,
     * seeded from the given place id. The seed itself is excluded from the result set, and
     * soft-deleted places never appear. With our tiny corpus the standard MLT defaults
     * (min_term_freq=2, min_doc_freq=5) return nothing, so we expose all three as tunables.
     */
    public PlaceSearchPageResponse moreLikeThis(Long seedId, int size) {
        String seedIdString = String.valueOf(seedId);

        Query mlt = Query.of(q -> q.moreLikeThis(m -> m
                .fields("name", "description", "pdfDescription")
                .like(l -> l.document(d -> d.index("places").id(seedIdString)))
                .minTermFreq(mltMinTermFreq)
                .minDocFreq(mltMinDocFreq)
                .maxQueryTerms(mltMaxQueryTerms)
        ));

        Query whole = Query.of(q -> q.bool(b -> b
                .filter(Query.of(qq -> qq.term(t -> t.field("deleted").value(false))))
                // Exclude the seed itself.
                .mustNot(Query.of(qq -> qq.ids(i -> i.values(seedIdString))))
                .must(mlt)
        ));

        NativeQuery nq = NativeQuery.builder()
                .withQuery(whole)
                .withPageable(PageRequest.of(0, size))
                .build();

        SearchHits<LocationIndex> hits = elasticsearchOperations.search(nq, LocationIndex.class);
        List<LocationSearchResultDTO> content = hits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .map(this::toResult)
                .collect(Collectors.toList());

        long total = hits.getTotalHits();
        int totalPages = size > 0 ? (int) Math.ceil((double) total / size) : 0;

        return PlaceSearchPageResponse.builder()
                .content(content)
                .totalElements(total)
                .totalPages(totalPages)
                .page(0)
                .size(size)
                .build();
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
