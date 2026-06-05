package rw.gov.utility_billing_system.utility.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PhoneValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPhone {

    String message() default "Phone number must be valid (e.g. +250788123456)";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
