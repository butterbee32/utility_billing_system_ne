package rw.gov.utility_billing_system.utility;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import rw.gov.utility_billing_system.enums.RoleName;

import java.math.BigDecimal;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    @Value("${app.mail.from-name:WASAC/REG Billing System}")
    private String fromName;

    @Value("${app.mail.login-url:http://localhost:8080/swagger-ui.html}")
    private String loginUrl;

    @Value("${app.mail.support-email:support@wasac.rw}")
    private String supportEmail;

    @Value("${app.otp.expiration-minutes:5}")
    private int otpExpirationMinutes;

    public boolean sendWelcomeEmail(String to, String fullNames) {
        return send(to, "Welcome to WASAC/REG Utility Billing System",
                "Dear " + fullNames + ",\n\n" +
                        "Welcome to the WASAC/REG Utility Billing System. " +
                        "Your account has been verified successfully.\n\n" +
                        "You can access the system here:\n" + loginUrl + "\n\n" +
                        "If you need help, contact us at " + supportEmail + ".\n\n" +
                        "Regards,\n" + fromName);
    }

    public boolean sendOtpEmail(String to, String otp) {
        return send(to, "WASAC/REG - OTP Verification Code",
                "Dear User,\n\n" +
                        "Your one-time verification code is:\n\n" +
                        "    " + otp + "\n\n" +
                        "This code expires in " + otpExpirationMinutes + " minutes.\n" +
                        "Do not share this code with anyone.\n\n" +
                        "If you did not request this code, please ignore this email.\n\n" +
                        "Regards,\n" + fromName);
    }

    public boolean sendEmailVerification(String to, String fullNames, String token) {
        return send(to, "WASAC/REG - Verify Your Email Address",
                "Dear " + fullNames + ",\n\n" +
                        "Please verify your email address using the token below:\n\n" +
                        "    " + token + "\n\n" +
                        "Alternatively, use the verify-email endpoint with your email and this token.\n\n" +
                        "Regards,\n" + fromName);
    }

    public boolean sendPasswordResetEmail(String to, String otp) {
        return send(to, "WASAC/REG - Password Reset Code",
                "Dear User,\n\n" +
                        "You requested a password reset. Your OTP code is:\n\n" +
                        "    " + otp + "\n\n" +
                        "This code expires in " + otpExpirationMinutes + " minutes.\n" +
                        "If you did not request a password reset, please ignore this email.\n\n" +
                        "Regards,\n" + fromName);
    }

    public boolean sendCustomerCredentialsEmail(String to, String fullNames, String tempPassword) {
        return send(to, "WASAC/REG - Your Customer Account Credentials",
                "Dear " + fullNames + ",\n\n" +
                        "Your customer account has been created on the WASAC/REG Utility Billing System.\n\n" +
                        "LOGIN DETAILS\n" +
                        "-------------\n" +
                        "Email (username) : " + to + "\n" +
                        "Temporary password: " + tempPassword + "\n\n" +
                        "ACCESS THE SYSTEM\n" +
                        "-----------------\n" +
                        loginUrl + "\n\n" +
                        "Please log in and change your password on first use.\n\n" +
                        "Regards,\n" + fromName);
    }

    public boolean sendTemporaryCredentialsEmail(String to, String fullNames, String tempPassword, RoleName role) {
        String roleLabel = formatRole(role);
        return send(to, "WASAC/REG - Your Staff Account Credentials",
                "Dear " + fullNames + ",\n\n" +
                        "Your staff account has been created on the WASAC/REG Utility Billing System.\n\n" +
                        "LOGIN DETAILS\n" +
                        "-------------\n" +
                        "Email (username) : " + to + "\n" +
                        "Role             : " + roleLabel + "\n" +
                        "Temporary password: " + tempPassword + "\n\n" +
                        "ACCESS THE SYSTEM\n" +
                        "-----------------\n" +
                        loginUrl + "\n\n" +
                        "FIRST LOGIN STEPS\n" +
                        "-----------------\n" +
                        "1. Log in using your email and temporary password above.\n" +
                        "2. You will be required to change your password immediately.\n" +
                        "3. After changing your password, you will have full access based on your role.\n\n" +
                        "SECURITY NOTICE\n" +
                        "---------------\n" +
                        "Do not share your credentials. Change your password as soon as you log in.\n\n" +
                        "If you did not expect this account, contact " + supportEmail + ".\n\n" +
                        "Regards,\n" + fromName);
    }

    public boolean sendAccountLockedEmail(String to, String fullNames) {
        return send(to, "WASAC/REG - Account Temporarily Locked",
                "Dear " + fullNames + ",\n\n" +
                        "Your account has been temporarily locked due to multiple failed login attempts.\n" +
                        "Please try again after 15 minutes.\n\n" +
                        "If this was not you, contact " + supportEmail + " immediately.\n\n" +
                        "Regards,\n" + fromName);
    }

    public boolean sendRoleAssignmentEmail(String to, String fullNames, Set<RoleName> roles, boolean isUpdate) {
        String action = isUpdate ? "updated" : "assigned";
        return send(to, "WASAC/REG - Role " + (isUpdate ? "Update" : "Assignment"),
                "Dear " + fullNames + ",\n\n" +
                        "Your system role has been " + action + " to: " + roles + "\n\n" +
                        "Access the system here: " + loginUrl + "\n\n" +
                        "Regards,\n" + fromName);
    }

    public boolean sendBillNotificationEmail(String to, String customerName, int month, int year, BigDecimal amount) {
        String body = String.format(
                "Dear %s,\n\n" +
                        "Your %02d/%d utility bill of %s FRW has been successfully processed.\n\n" +
                        "Please log in to view and pay your bill:\n%s\n\n" +
                        "Regards,\n%s",
                customerName, month, year, amount.toPlainString(), loginUrl, fromName);
        return send(to, "WASAC/REG - Utility Bill Notification", body);
    }

    public boolean sendPaymentConfirmationEmail(String to, String customerName, BigDecimal amount, String billRef) {
        return send(to, "WASAC/REG - Payment Confirmation",
                "Dear " + customerName + ",\n\n" +
                        "We have received your payment of " + amount.toPlainString() +
                        " FRW for bill reference " + billRef + ".\n\n" +
                        "Thank you for your payment.\n\n" +
                        "Regards,\n" + fromName);
    }

    private String formatRole(RoleName role) {
        return switch (role) {
            case ROLE_ADMIN -> "Administrator";
            case ROLE_OPERATOR -> "Operator";
            case ROLE_FINANCE -> "Finance Officer";
            case ROLE_CUSTOMER -> "Customer";
        };
    }

    private boolean send(String to, String subject, String body) {
        if (fromEmail == null || fromEmail.isBlank()) {
            log.info("Email not configured. Would send to {} from {}: {} - {}", to, fromName, subject, body);
            return true;
        }
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "UTF-8");
            helper.setFrom(fromEmail, fromName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, false);
            mailSender.send(mimeMessage);
            return true;
        } catch (Exception ex) {
            log.warn("Failed to send email to {}: {}", to, ex.getMessage());
            return false;
        }
    }
}
