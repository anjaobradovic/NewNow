package rs.ftn.newnow.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventRatingDTO {
    private Long eventId;
    private String eventName;
    private Double averageRating;
    private Long reviewCount;
}
