package rs.ftn.newnow.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class NameValidator implements ConstraintValidator<ValidName, String> {

    private int minLength;

    @Override
    public void initialize(ValidName constraintAnnotation) {
        this.minLength = constraintAnnotation.minLength();
    }

    @Override
    public boolean isValid(String name, ConstraintValidatorContext context) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }

        // Check minimum length
        if (name.trim().length() < minLength) {
            return false;
        }

        // Only letters (including Serbian characters), spaces, apostrophes, and hyphens
        // Serbian characters: čćžšđČĆŽŠĐ
        String nameRegex = "^[a-zA-ZčćžšđČĆŽŠĐ\\s'\\-]+$";
        return name.matches(nameRegex);
    }
}
