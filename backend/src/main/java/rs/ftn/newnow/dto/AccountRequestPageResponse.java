package rs.ftn.newnow.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountRequestPageResponse {
    private List<AccountRequestDTO> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
}
