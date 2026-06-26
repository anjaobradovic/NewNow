package rs.ftn.newnow.search.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationSearchResultDTO {
    private Long id;
    private String name;
    private String description;
    private String address;
    private String type;
    private Integer reviewCount;
    private Double totalRating;
    private Double avgPerformance;
    private Double avgSoundAndLighting;
    private Double avgVenue;
    private Double avgOverallImpression;
    private String imageUrl;
    private Boolean hasPdf;
    /** Highlighted fragments per matched field; absent when no clauses produced highlights. */
    private Map<String, List<String>> highlights;
}
