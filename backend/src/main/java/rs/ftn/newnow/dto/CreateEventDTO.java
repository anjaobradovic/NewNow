package rs.ftn.newnow.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateEventDTO {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Address is required")
    private String address;

    @NotBlank(message = "Type is required")
    private String type;

    @NotNull(message = "Date is required")
    private LocalDate date;

    private Double price = 0.0;

    private Boolean recurrent = false;

    @NotNull(message = "Free flag is required")
    private Boolean free = Boolean.FALSE;

    @AssertTrue(message = "Either set free=true with price=0, or free=false with price>0")
    public boolean isPriceFreeConsistent() {
        if (free == null) {
            return false;
        }
        double p = price == null ? 0.0 : price;
        return free ? p == 0.0 : p > 0.0;
    }
}
