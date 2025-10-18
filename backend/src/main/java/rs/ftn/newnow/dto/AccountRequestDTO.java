package rs.ftn.newnow.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import rs.ftn.newnow.model.enums.RequestStatus;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountRequestDTO {
    
    private Long id;
    private String email;
    private String name;
    private String phoneNumber;
    private LocalDate birthday;
    private String address;
    private String city;
    private RequestStatus status;
    private LocalDate createdAt;
    private String rejectionReason;
}
