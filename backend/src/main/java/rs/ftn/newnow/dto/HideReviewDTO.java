package rs.ftn.newnow.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HideReviewDTO {
    
    @NotNull(message = "Hidden status is required")
    private Boolean hidden;
}
