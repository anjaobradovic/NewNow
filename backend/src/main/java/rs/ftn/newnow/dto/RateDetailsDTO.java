package rs.ftn.newnow.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RateDetailsDTO {
    private Integer performance;
    private Integer soundAndLighting;
    private Integer venue;
    private Integer overallImpression;
    private Double average;
}
