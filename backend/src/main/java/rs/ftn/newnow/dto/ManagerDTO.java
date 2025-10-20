package rs.ftn.newnow.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ManagerDTO {

    private Long userId;
    private String name;
    private String email;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean active;
}
