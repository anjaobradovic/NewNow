package rs.ftn.newnow.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCommentDTO {

    @NotBlank(message = "Comment text is required")
    private String text;

    private Long parentCommentId;
}
