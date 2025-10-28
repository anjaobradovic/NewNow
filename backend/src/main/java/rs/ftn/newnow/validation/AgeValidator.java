package rs.ftn.newnow.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;
import java.time.Period;

public class AgeValidator implements ConstraintValidator<ValidAge, LocalDate> {

    private int minAge;
    private int maxAge;
    private boolean required;

    @Override
    public void initialize(ValidAge constraintAnnotation) {
        this.minAge = constraintAnnotation.minAge();
        this.maxAge = constraintAnnotation.maxAge();
        this.required = constraintAnnotation.required();
    }

    @Override
    public boolean isValid(LocalDate birthday, ConstraintValidatorContext context) {
        // If not required and null, it's valid
        if (!required && birthday == null) {
            return true;
        }

        // If required and null, it's invalid
        if (required && birthday == null) {
            return false;
        }

        // Cannot be in the future
        if (birthday.isAfter(LocalDate.now())) {
            return false;
        }

        // Calculate age
        int age = Period.between(birthday, LocalDate.now()).getYears();

        // Check age range
        return age >= minAge && age <= maxAge;
    }
}
