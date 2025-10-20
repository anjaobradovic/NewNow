package rs.ftn.newnow.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ManagedLocationDTO {
    private Long id;
    private String locationName;
    private String locationAddress;
    private String locationType;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean isActive;
}
