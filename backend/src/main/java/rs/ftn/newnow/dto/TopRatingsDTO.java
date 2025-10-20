package rs.ftn.newnow.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TopRatingsDTO {
    private List<EventRatingDTO> topEvents;
    private LocationRatingDTO locationRating;
}
