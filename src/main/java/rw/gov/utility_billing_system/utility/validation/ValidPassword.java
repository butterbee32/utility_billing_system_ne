package rw.gov.utility_billing_system.utility.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PasswordValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPassword {

    String message() default "Password must be at least 8 characters with uppercase, lowercase, digit and special character";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
