package rw.gov.utility_billing_system.utility.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = NationalIdValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidNationalId {

    String message() default "National ID must be exactly 16 digits";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
