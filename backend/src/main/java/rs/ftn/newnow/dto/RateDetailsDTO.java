package rs.ftn.newnow.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RateDetailsDTO {
    private Integer performance;
    private Integer soundLight;
    private Integer space;
    private Integer overall;
    private Double average;
}
