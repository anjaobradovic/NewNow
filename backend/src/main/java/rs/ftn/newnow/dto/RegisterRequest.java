package rs.ftn.newnow.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import rs.ftn.newnow.validation.ValidAge;
import rs.ftn.newnow.validation.ValidName;
import rs.ftn.newnow.validation.ValidPassword;
import rs.ftn.newnow.validation.ValidPhoneNumber;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Password is required")
    @ValidPassword
    private String password;

    @NotBlank(message = "Name is required")
    @ValidName(minLength = 2)
    private String name;

    @NotBlank(message = "Phone number is required")
    @ValidPhoneNumber(required = true)
    private String phoneNumber;

    @NotNull(message = "Birthday is required")
    @ValidAge(minAge = 13, maxAge = 120, required = true)
    private LocalDate birthday;

    @NotBlank(message = "Address is required")
    @Size(min = 5, message = "Address must be at least 5 characters")
    private String address;

    @NotBlank(message = "City is required")
    @ValidName(minLength = 2, message = "City must be at least 2 characters and contain only letters")
    private String city;
}
