package rs.ftn.newnow.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RateDTO {
    private Long id;
    private Integer performance;
    private Integer soundAndLighting;
    private Integer venue;
    private Integer overallImpression;
    private Double averageRating;
}
