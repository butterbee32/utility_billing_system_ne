package rw.gov.utility_billing_system.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.gov.utility_billing_system.dto.request.auth.*;
import rw.gov.utility_billing_system.dto.response.auth.AuthResponse;
import rw.gov.utility_billing_system.entity.TokenBlacklist;
import rw.gov.utility_billing_system.entity.User;
import rw.gov.utility_billing_system.enums.AuditAction;
import rw.gov.utility_billing_system.enums.NotificationType;
import rw.gov.utility_billing_system.enums.OtpType;
import rw.gov.utility_billing_system.enums.RoleName;
import rw.gov.utility_billing_system.enums.Status;
import rw.gov.utility_billing_system.exception.*;
import rw.gov.utility_billing_system.repository.CustomerRepository;
import rw.gov.utility_billing_system.repository.TokenBlacklistRepository;
import rw.gov.utility_billing_system.repository.UserRepository;
import rw.gov.utility_billing_system.security.JwtTokenProvider;
import rw.gov.utility_billing_system.security.UserPrincipal;
import rw.gov.utility_billing_system.security.SecurityUtils;
import rw.gov.utility_billing_system.utility.EmailService;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final TokenBlacklistRepository tokenBlacklistRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final EmailService emailService;
    private final AuditLogService auditLogService;
    private final OtpService otpService;
    private final NotificationDispatchService notificationDispatchService;

    @Value("${app.security.max-failed-attempts:5}")
    private int maxFailedAttempts;

    @Value("${app.security.lock-duration-minutes:15}")
    private int lockDurationMinutes;

    @Value("${app.email-verification.expiration-hours:24}")
    private int emailVerificationExpirationHours;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (!request.getRoles().equals(Set.of(RoleName.ROLE_CUSTOMER))) {
            throw new BadRequestException("Public registration is only allowed for customers");
        }
        String email = request.getEmail().toLowerCase();
        validateUniqueUser(email, request.getPhoneNumber());

        String verificationToken = UUID.randomUUID().toString();
        User user = User.builder()
                .fullNames(request.getFullNames())
                .email(email)
                .phoneNumber(request.getPhoneNumber())
                .password(passwordEncoder.encode(request.getPassword()))
                .status(Status.INACTIVE)
                .emailVerified(false)
                .emailVerificationToken(verificationToken)
                .emailVerificationExpiry(LocalDateTime.now().plusHours(emailVerificationExpirationHours))
                .roles(request.getRoles())
                .build();
        userRepository.save(user);

        String otp = otpService.generateOtp(email, OtpType.REGISTRATION);
        emailService.sendOtpEmail(email, otp);
        emailService.sendEmailVerification(email, user.getFullNames(), verificationToken);

        notificationDispatchService.dispatchToUser(user,
                "OTP generated for registration. Please verify your account.",
                NotificationType.OTP_GENERATED,
                () -> emailService.sendOtpEmail(email, otp));

        auditLogService.log(AuditAction.CREATE, "User", user.getId(), "User registered, pending OTP verification");

        return AuthResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .fullNames(user.getFullNames())
                .roles(user.getRoles())
                .mustChangePassword(false)
                .message("Registration successful. Please verify OTP and email.")
                .build();
    }

    @Transactional
    public AuthResponse verifyOtp(VerifyOtpRequest request) {
        String email = request.getEmail().toLowerCase();
        otpService.validateAndConsume(email, OtpType.REGISTRATION, request.getOtpCode());

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setEmailVerified(true);
        user.setStatus(Status.ACTIVE);
        userRepository.save(user);

        customerRepository.findByEmail(email).ifPresent(customer -> {
            customer.setAccountVerified(true);
            customer.setStatus(Status.ACTIVE);
            customerRepository.save(customer);
        });

        emailService.sendWelcomeEmail(email, user.getFullNames());
        auditLogService.log(AuditAction.UPDATE, "User", user.getId(), "OTP verified");

        return buildAuthResponse(user, null, "Email verified successfully. You can now login.");
    }

    @Transactional
    public AuthResponse verifyEmail(VerifyEmailRequest request) {
        String email = request.getEmail().toLowerCase();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (user.getEmailVerificationToken() == null
                || !user.getEmailVerificationToken().equals(request.getVerificationToken())) {
            throw new BadRequestException("Invalid email verification token");
        }
        if (user.getEmailVerificationExpiry() != null
                && user.getEmailVerificationExpiry().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Email verification token has expired");
        }

        user.setEmailVerified(true);
        user.setStatus(Status.ACTIVE);
        user.setEmailVerificationToken(null);
        user.setEmailVerificationExpiry(null);
        userRepository.save(user);

        emailService.sendWelcomeEmail(email, user.getFullNames());
        auditLogService.log(AuditAction.UPDATE, "User", user.getId(), "Email verified via token");

        return buildAuthResponse(user, null, "Email verified successfully.");
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        String email = request.getEmail().toLowerCase();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        checkAccountLock(user);

        if (!user.isEmailVerified()) {
            throw new UnauthorizedException("Email not verified. Please verify OTP or email first.");
        }
        if (user.getStatus() != Status.ACTIVE) {
            throw new UnauthorizedException("Account is inactive");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            handleFailedLogin(user);
            throw new UnauthorizedException("Invalid email or password");
        }

        resetLoginAttempts(user);
        user.setLastLoginAt(LocalDateTime.now());
        if (user.isMustChangePassword()) {
            user.setTemporaryPasswordUsed(true);
        }
        userRepository.save(user);

        UserPrincipal principal = new UserPrincipal(user);
        String token = jwtTokenProvider.generateToken(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        principal, null, principal.getAuthorities()));

        auditLogService.log(AuditAction.LOGIN, "User", user.getId(), "User logged in");

        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .userId(user.getId())
                .email(user.getEmail())
                .fullNames(user.getFullNames())
                .roles(user.getRoles())
                .mustChangePassword(user.isMustChangePassword())
                .message(user.isMustChangePassword()
                        ? "Login successful. You must change your password."
                        : "Login successful")
                .build();
    }

    @Transactional
    public AuthResponse changePassword(ChangePasswordRequest request) {
        UserPrincipal principal = SecurityUtils.getCurrentUser();
        if (principal == null) {
            throw new UnauthorizedException("Not authenticated");
        }
        User user = userRepository.findByEmail(principal.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new UnauthorizedException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setMustChangePassword(false);
        user.setTemporaryPasswordUsed(false);
        userRepository.save(user);

        auditLogService.log(AuditAction.UPDATE, "User", user.getId(), "Password changed");

        return AuthResponse.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .fullNames(user.getFullNames())
                .roles(user.getRoles())
                .mustChangePassword(false)
                .message("Password changed successfully")
                .build();
    }

    @Transactional
    public void logout(String token) {
        if (token != null && !token.isBlank()) {
            tokenBlacklistRepository.save(TokenBlacklist.builder().token(token).build());
            UserPrincipal principal = SecurityUtils.getCurrentUser();
            if (principal != null) {
                auditLogService.log(AuditAction.LOGOUT, "User", principal.getId(), "User logged out");
            }
        }
    }

    @Transactional
    public AuthResponse forgotPassword(ForgotPasswordRequest request) {
        String email = request.getEmail().toLowerCase();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        String otp = otpService.generateOtp(email, OtpType.PASSWORD_RESET);
        emailService.sendPasswordResetEmail(email, otp);

        notificationDispatchService.dispatchToUser(user,
                "Password reset OTP has been sent to your email.",
                NotificationType.PASSWORD_RESET,
                () -> emailService.sendPasswordResetEmail(email, otp));

        return AuthResponse.builder()
                .email(email)
                .message("Password reset OTP sent to your email")
                .build();
    }

    @Transactional
    public AuthResponse resetPassword(ResetPasswordRequest request) {
        String email = request.getEmail().toLowerCase();
        otpService.validateAndConsume(email, OtpType.PASSWORD_RESET, request.getOtpCode());

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setMustChangePassword(false);
        userRepository.save(user);

        auditLogService.log(AuditAction.UPDATE, "User", user.getId(), "Password reset");

        return AuthResponse.builder()
                .email(email)
                .message("Password reset successful")
                .build();
    }

    private void checkAccountLock(User user) {
        if (user.isAccountLocked()) {
            if (user.getAccountLockedUntil() != null && user.getAccountLockedUntil().isAfter(LocalDateTime.now())) {
                throw new UnauthorizedException("Account is locked. Try again after "
                        + user.getAccountLockedUntil());
            }
            user.setAccountLocked(false);
            user.setFailedLoginAttempts(0);
            user.setAccountLockedUntil(null);
            userRepository.save(user);
        }
    }

    private void handleFailedLogin(User user) {
        int attempts = user.getFailedLoginAttempts() + 1;
        user.setFailedLoginAttempts(attempts);
        if (attempts >= maxFailedAttempts) {
            user.setAccountLocked(true);
            user.setAccountLockedUntil(LocalDateTime.now().plusMinutes(lockDurationMinutes));
            emailService.sendAccountLockedEmail(user.getEmail(), user.getFullNames());
            notificationDispatchService.dispatchToUser(user,
                    "Your account has been locked due to multiple failed login attempts.",
                    NotificationType.ACCOUNT_LOCKED,
                    () -> emailService.sendAccountLockedEmail(user.getEmail(), user.getFullNames()));
        }
        userRepository.save(user);
    }

    private void resetLoginAttempts(User user) {
        user.setFailedLoginAttempts(0);
        user.setAccountLocked(false);
        user.setAccountLockedUntil(null);
    }

    private void validateUniqueUser(String email, String phone) {
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("Email already registered");
        }
        if (userRepository.existsByPhoneNumber(phone)) {
            throw new DuplicateResourceException("Phone number already registered");
        }
    }

    private AuthResponse buildAuthResponse(User user, String token, String message) {
        return AuthResponse.builder()
                .token(token)
                .type(token != null ? "Bearer" : null)
                .userId(user.getId())
                .email(user.getEmail())
                .fullNames(user.getFullNames())
                .roles(user.getRoles())
                .mustChangePassword(user.isMustChangePassword())
                .message(message)
                .build();
    }
}
