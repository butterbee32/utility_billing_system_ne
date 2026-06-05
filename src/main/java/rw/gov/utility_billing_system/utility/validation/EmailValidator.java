package rw.gov.utility_billing_system.utility.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Set;
import java.util.regex.Pattern;

public class EmailValidator implements ConstraintValidator<ValidEmail, String> {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,}$");
    private static final Pattern PHONE_LIKE = Pattern.compile("^\\+?[0-9]{8,15}$");
    private static final Pattern PASSWORD_LIKE = Pattern.compile(
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#]).{8,}$");
    private static final Set<String> REJECTED_NAME_LOCAL_PARTS = Set.of(
            "name", "firstname", "lastname", "username", "fullname", "john", "jane");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return false;
        }
        String email = value.trim().toLowerCase();
        if (!email.equals(value)) {
            return false;
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            return false;
        }
        if (PHONE_LIKE.matcher(email).matches()) {
            return false;
        }
        if (PASSWORD_LIKE.matcher(email).matches()) {
            return false;
        }
        String localPart = email.substring(0, email.indexOf('@'));
        return !REJECTED_NAME_LOCAL_PARTS.contains(localPart);
    }
}
