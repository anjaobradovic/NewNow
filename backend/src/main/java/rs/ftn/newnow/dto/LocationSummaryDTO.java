package rs.ftn.newnow.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationSummaryDTO {
    private Long locationId;
    private String locationName;
    private String period;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long totalEvents;
    private Long totalReviews;
    private Double averageRating;
    private Long totalVisitors;
}
