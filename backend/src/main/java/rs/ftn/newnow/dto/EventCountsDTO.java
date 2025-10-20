package rs.ftn.newnow.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventCountsDTO {
    private Long totalEvents;
    private Long regularEvents;
    private Long nonRegularEvents;
    private Long freeEvents;
    private Long paidEvents;
}
