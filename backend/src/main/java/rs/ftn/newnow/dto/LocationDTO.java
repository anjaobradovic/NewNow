package rs.ftn.newnow.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationDTO {

    private Long id;
    private String name;
    private String description;
    private String address;
    private Double totalRating;
    private String type;
    private String imageUrl;
}
