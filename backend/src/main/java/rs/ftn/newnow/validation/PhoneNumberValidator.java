package rs.ftn.newnow.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PhoneNumberValidator implements ConstraintValidator<ValidPhoneNumber, String> {

    private boolean required;

    @Override
    public void initialize(ValidPhoneNumber constraintAnnotation) {
        this.required = constraintAnnotation.required();
    }

    @Override
    public boolean isValid(String phoneNumber, ConstraintValidatorContext context) {
        // If not required and null/empty, it's valid
        if (!required && (phoneNumber == null || phoneNumber.trim().isEmpty())) {
            return true;
        }

        // If required and null/empty, it's invalid
        if (required && (phoneNumber == null || phoneNumber.trim().isEmpty())) {
            return false;
        }

        // Remove spaces, dashes, parentheses for validation
        String cleanedPhone = phoneNumber.replaceAll("[\\s\\-\\(\\)]", "");

        // Must contain only digits and optionally + at the beginning
        if (!cleanedPhone.matches("^[\\+]?[0-9]+$")) {
            return false;
        }

        // Remove + to count digits
        String digitsOnly = cleanedPhone.replace("+", "");

        // Must be between 8 and 15 digits
        if (digitsOnly.length() < 8 || digitsOnly.length() > 15) {
            return false;
        }

        // Additional validation for Serbian phone numbers
        if (cleanedPhone.startsWith("+381")) {
            String afterPrefix = cleanedPhone.substring(4);
            return afterPrefix.length() >= 8 && afterPrefix.length() <= 9;
        } else if (cleanedPhone.startsWith("381")) {
            String afterPrefix = cleanedPhone.substring(3);
            return afterPrefix.length() >= 8 && afterPrefix.length() <= 9;
        } else if (cleanedPhone.startsWith("0")) {
            return digitsOnly.length() >= 9 && digitsOnly.length() <= 10;
        }

        // For other country codes, just check the length
        return true;
    }
}
