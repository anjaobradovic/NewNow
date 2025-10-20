package rs.ftn.newnow.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDTO {
    private Long id;
    private LocalDateTime createdAt;
    private Integer eventCount;
    private Boolean hidden;
    private Long locationId;
    private String locationName;
    private Long eventId;
    private String eventName;
    private RateDTO rate;
}
