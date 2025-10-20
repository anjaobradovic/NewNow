package rs.ftn.newnow.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventBasicDTO {
    private Long id;
    private String name;
    private String type;
    private LocalDate date;
    private Boolean recurrent;
}
