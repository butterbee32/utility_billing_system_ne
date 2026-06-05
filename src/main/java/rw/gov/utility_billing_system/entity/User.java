package rw.gov.utility_billing_system.entity;

import jakarta.persistence.*;
import lombok.*;
import rw.gov.utility_billing_system.entity.base.AuditableEntity;
import rw.gov.utility_billing_system.enums.RoleName;
import rw.gov.utility_billing_system.enums.Status;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullNames;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String phoneNumber;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(nullable = false)
    @Builder.Default
    private boolean emailVerified = false;

    @Column(nullable = false)
    @Builder.Default
    private boolean mustChangePassword = false;

    @Column(nullable = false)
    @Builder.Default
    private boolean temporaryPasswordUsed = false;

    @Column(nullable = false)
    @Builder.Default
    private int failedLoginAttempts = 0;

    @Column(nullable = false)
    @Builder.Default
    private boolean accountLocked = false;

    private LocalDateTime accountLockedUntil;

    private LocalDateTime lastLoginAt;

    private String emailVerificationToken;

    private LocalDateTime emailVerificationExpiry;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    @Builder.Default
    private Set<RoleName> roles = new HashSet<>();

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private Customer customer;
}
