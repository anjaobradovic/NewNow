package rs.ftn.newnow.search.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaceSearchPageResponse {
    private List<LocationSearchResultDTO> content;
    private long totalElements;
    private int totalPages;
    private int page;
    private int size;
}
