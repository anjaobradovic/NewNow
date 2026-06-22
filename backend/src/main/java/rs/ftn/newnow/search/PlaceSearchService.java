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

    public PlaceSearchPageResponse search(String name,
                                          String description,
                                          String pdf,
                                          Integer reviewsFrom,
                                          Integer reviewsTo,
                                          int page,
                                          int size) {

        BoolQuery.Builder bool = new BoolQuery.Builder();
        // Always exclude soft-deleted places.
        bool.filter(Query.of(q -> q.term(t -> t.field("deleted").value(false))));

        boolean any = false;
        if (name != null && !name.isBlank()) {
            bool.must(Query.of(q -> q.match(m -> m.field("name").query(name.trim()))));
            any = true;
        }
        if (description != null && !description.isBlank()) {
            bool.must(Query.of(q -> q.match(m -> m.field("description").query(description.trim()))));
            any = true;
        }
        if (pdf != null && !pdf.isBlank()) {
            bool.must(Query.of(q -> q.match(m -> m.field("pdfDescription").query(pdf.trim()))));
            any = true;
        }
        if (reviewsFrom != null || reviewsTo != null) {
            final Integer from = reviewsFrom;
            final Integer to = reviewsTo;
            bool.must(Query.of(q -> q.range(r -> r.number(n -> {
                n.field("reviewCount");
                if (from != null) n.gte(from.doubleValue());
                if (to != null) n.lte(to.doubleValue());
                return n;
            }))));
            any = true;
        }
        if (!any) {
            // No filters supplied → return everything (non-deleted).
            bool.must(Query.of(q -> q.matchAll(m -> m)));
        }

        NativeQuery nq = NativeQuery.builder()
                .withQuery(Query.of(q -> q.bool(bool.build())))
                .withPageable(PageRequest.of(page, size))
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
                .page(page)
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
                .imageUrl(idx.getImageUrl())
                .hasPdf(idx.getPdfObjectKey() != null)
                .build();
    }
}
