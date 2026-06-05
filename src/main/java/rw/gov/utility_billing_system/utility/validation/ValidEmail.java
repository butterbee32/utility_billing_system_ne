package rw.gov.utility_billing_system.utility.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = EmailValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidEmail {

    String message() default "Email must be valid and lowercase";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
