package rs.ftn.newnow.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentDTO {
    private Long id;
    private String text;
    private LocalDateTime createdAt;
    private UserBasicDTO author;
    private Long parentCommentId;
    private List<CommentDTO> replies;
}
