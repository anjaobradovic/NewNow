package rs.ftn.newnow.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import rs.ftn.newnow.model.enums.RequestStatus;

import java.time.LocalDate;

@Entity
@Table(name = "account_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Email
    @NotBlank
    @Column(unique = true, nullable = false)
    private String email;

    @NotBlank
    @Column(nullable = false)
    private String password;

    @NotBlank
    @Column(nullable = false)
    private String name;

    private String phoneNumber;

    private LocalDate birthday;

    @NotBlank
    @Column(nullable = false)
    private String address;

    private String city;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStatus status = RequestStatus.PENDING;

    @Column(nullable = false)
    private LocalDate createdAt;

    @Column(columnDefinition = "TEXT")
    private String rejectionReason;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDate.now();
        }
        if (status == null) {
            status = RequestStatus.PENDING;
        }
    }
}
