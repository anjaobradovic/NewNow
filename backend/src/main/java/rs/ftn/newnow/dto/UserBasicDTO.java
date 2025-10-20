package rs.ftn.newnow.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserBasicDTO {
    private Long id;
    private String name;
    private String email;
}
