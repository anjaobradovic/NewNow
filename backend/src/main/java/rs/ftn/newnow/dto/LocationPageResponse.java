package rs.ftn.newnow.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationPageResponse {

    private List<LocationDTO> locations;
    private int currentPage;
    private int totalPages;
    private long totalElements;
}
