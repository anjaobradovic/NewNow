package rs.ftn.newnow.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogPageResponse {
    private List<AuditLogDTO> logs;
    private int currentPage;
    private int totalPages;
    private long totalItems;
}
