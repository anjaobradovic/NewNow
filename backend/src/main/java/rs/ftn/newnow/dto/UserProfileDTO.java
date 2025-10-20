package rs.ftn.newnow.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDTO {
    private Long id;
    private String email;
    private String name;
    private String phoneNumber;
    private LocalDate birthday;
    private String address;
    private String city;
    private LocalDate createdAt;
    private Set<String> roles;
    private String avatarUrl;
}
