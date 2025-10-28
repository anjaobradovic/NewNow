package rs.ftn.newnow.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = AgeValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidAge {
    String message() default "You must be between 13 and 120 years old";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    int minAge() default 13;
    int maxAge() default 120;
    boolean required() default false;
}
