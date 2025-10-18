package rs.ftn.newnow.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventDTO {

    private Long id;
    private String name;
    private String address;
    private String type;
    private LocalDate date;
    private Double price;
    private Boolean recurrent;
    private Long locationId;
    private String locationName;
    private String imageUrl;
}
