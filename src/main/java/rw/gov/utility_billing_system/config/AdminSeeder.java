package rw.gov.utility_billing_system.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import rw.gov.utility_billing_system.entity.User;
import rw.gov.utility_billing_system.enums.RoleName;
import rw.gov.utility_billing_system.enums.Status;
import rw.gov.utility_billing_system.repository.UserRepository;

import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Value("${app.admin.full-names}")
    private String adminFullNames;

    @Value("${app.admin.phone}")
    private String adminPhone;

    @Override
    public void run(String... args) {
        if (!userRepository.existsByEmail(adminEmail.toLowerCase())) {
            User admin = User.builder()
                    .fullNames(adminFullNames)
                    .email(adminEmail.toLowerCase())
                    .phoneNumber(adminPhone)
                    .password(passwordEncoder.encode(adminPassword))
                    .status(Status.ACTIVE)
                    .emailVerified(true)
                    .roles(Set.of(RoleName.ROLE_ADMIN))
                    .build();
            userRepository.save(admin);
            log.info("Default admin user seeded: {}", adminEmail);
        }
    }
}
