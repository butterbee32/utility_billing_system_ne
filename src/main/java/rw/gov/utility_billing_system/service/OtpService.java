package rw.gov.utility_billing_system.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.gov.utility_billing_system.entity.OtpToken;
import rw.gov.utility_billing_system.enums.OtpType;
import rw.gov.utility_billing_system.exception.BadRequestException;
import rw.gov.utility_billing_system.exception.OtpException;
import rw.gov.utility_billing_system.repository.OtpTokenRepository;
import rw.gov.utility_billing_system.utility.OtpGenerator;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OtpService {

    private final OtpTokenRepository otpTokenRepository;

    @Value("${app.otp.expiration-minutes:5}")
    private int otpExpirationMinutes;

    @Value("${app.otp.max-attempts:3}")
    private int maxAttempts;

    @Value("${app.otp.max-requests-per-window:3}")
    private int maxRequestsPerWindow;

    @Value("${app.otp.request-window-minutes:10}")
    private int requestWindowMinutes;

    @Transactional
    public String generateOtp(String email, OtpType type) {
        LocalDateTime windowStart = LocalDateTime.now().minusMinutes(requestWindowMinutes);
        int recentCount = otpTokenRepository.countByEmailAndOtpTypeAndGeneratedAtAfter(email, type, windowStart);
        if (recentCount >= maxRequestsPerWindow) {
            throw new BadRequestException("Maximum OTP requests reached. Try again later.");
        }

        String otp = OtpGenerator.generate();
        otpTokenRepository.save(OtpToken.builder()
                .email(email)
                .otpCode(otp)
                .otpType(type)
                .generatedAt(LocalDateTime.now())
                .expiryTime(LocalDateTime.now().plusMinutes(otpExpirationMinutes))
                .build());
        return otp;
    }

    @Transactional
    public void validateAndConsume(String email, OtpType type, String code) {
        OtpToken token = otpTokenRepository
                .findTopByEmailAndOtpTypeAndUsedFalseOrderByGeneratedAtDesc(email, type)
                .orElseThrow(() -> new OtpException("No OTP found for this email"));

        if (token.isUsed()) {
            throw new OtpException("OTP already used");
        }
        if (token.getExpiryTime().isBefore(LocalDateTime.now())) {
            throw new OtpException("OTP has expired");
        }
        if (token.getAttempts() >= maxAttempts) {
            throw new OtpException("Maximum OTP verification attempts exceeded");
        }

        if (!token.getOtpCode().equals(code)) {
            token.setAttempts(token.getAttempts() + 1);
            otpTokenRepository.save(token);
            throw new OtpException("Invalid OTP code");
        }

        token.setUsed(true);
        otpTokenRepository.save(token);
    }
}
