package rs.ftn.newnow.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationDetailsDTO {

    private Long id;
    private String name;
    private String description;
    private String address;
    private String type;
    private LocalDate createdAt;
    private String imageUrl;
    private Double averageRating;
    private Integer totalReviews;
    private List<EventDTO> upcomingEvents;
}
