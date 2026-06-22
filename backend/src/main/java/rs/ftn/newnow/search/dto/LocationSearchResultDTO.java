package rs.ftn.newnow.search.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private String imageUrl;
    private Boolean hasPdf;
}
