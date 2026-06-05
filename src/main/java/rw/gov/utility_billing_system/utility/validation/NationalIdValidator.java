package rw.gov.utility_billing_system.utility.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class NationalIdValidator implements ConstraintValidator<ValidNationalId, String> {

    private static final Pattern NATIONAL_ID_PATTERN = Pattern.compile("^[0-9]{16}$");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return false;
        }
        return NATIONAL_ID_PATTERN.matcher(value.trim()).matches();
    }
}
