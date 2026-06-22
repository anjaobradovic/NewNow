package rs.ftn.newnow.dto;

import jakarta.validation.constraints.AssertTrue;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEventDTO {

    private String name;
    private String address;
    private String type;
    private LocalDate date;
    private Double price;
    private Boolean recurrent;
    private Boolean free;

    @AssertTrue(message = "If free and price are both supplied, free=true requires price=0 and free=false requires price>0")
    public boolean isPriceFreeConsistent() {
        if (free == null || price == null) {
            return true;
        }
        return free ? price == 0.0 : price > 0.0;
    }
}
