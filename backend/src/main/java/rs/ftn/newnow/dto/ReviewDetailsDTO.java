package rs.ftn.newnow.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDetailsDTO {
    private Long id;
    private LocalDateTime createdAt;
    private String comment;
    private Integer eventCount;
    private Boolean hidden;
    private UserBasicDTO author;
    private EventBasicDTO event;
    private RateDetailsDTO ratings;
}
