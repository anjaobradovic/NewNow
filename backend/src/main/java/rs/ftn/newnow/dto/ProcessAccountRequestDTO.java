package rs.ftn.newnow.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProcessAccountRequestDTO {
    
    @NotNull(message = "Request ID is required")
    private Long requestId;
    
    @NotNull(message = "Approved flag is required")
    private Boolean approved;
    
    private String rejectionReason;
}
