package rw.gov.utility_billing_system.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.gov.utility_billing_system.dto.request.user.RoleUpdateRequest;
import rw.gov.utility_billing_system.dto.request.user.StaffCreateRequest;
import rw.gov.utility_billing_system.dto.request.user.StatusUpdateRequest;
import rw.gov.utility_billing_system.dto.request.user.UserRequest;
import rw.gov.utility_billing_system.dto.response.PageResponse;
import rw.gov.utility_billing_system.dto.response.user.UserResponse;
import rw.gov.utility_billing_system.entity.User;
import rw.gov.utility_billing_system.enums.AuditAction;
import rw.gov.utility_billing_system.enums.NotificationType;
import rw.gov.utility_billing_system.enums.RoleName;
import rw.gov.utility_billing_system.enums.Status;
import rw.gov.utility_billing_system.exception.BadRequestException;
import rw.gov.utility_billing_system.exception.DuplicateResourceException;
import rw.gov.utility_billing_system.exception.ResourceNotFoundException;
import rw.gov.utility_billing_system.mapper.EntityMapper;
import rw.gov.utility_billing_system.repository.UserRepository;
import rw.gov.utility_billing_system.utility.EmailService;
import rw.gov.utility_billing_system.utility.TemporaryPasswordGenerator;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;
    private final EmailService emailService;
    private final NotificationDispatchService notificationDispatchService;

    @Transactional
    public UserResponse create(UserRequest request) {
        String email = request.getEmail().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("Email already exists");
        }
        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new DuplicateResourceException("Phone number already exists");
        }
        User user = User.builder()
                .fullNames(request.getFullNames())
                .email(email)
                .phoneNumber(request.getPhoneNumber())
                .password(passwordEncoder.encode(request.getPassword()))
                .status(request.getStatus() != null ? request.getStatus() : Status.ACTIVE)
                .emailVerified(true)
                .roles(request.getRoles())
                .build();
        userRepository.save(user);
        auditLogService.log(AuditAction.CREATE, "User", user.getId(), "Admin created user");
        return EntityMapper.toUserResponse(user);
    }

    @Transactional
    public UserResponse createStaff(StaffCreateRequest request) {
        if (request.getRole() != RoleName.ROLE_OPERATOR && request.getRole() != RoleName.ROLE_FINANCE) {
            throw new BadRequestException("Staff can only be OPERATOR or FINANCE");
        }
        String email = request.getEmail().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("Email already exists");
        }
        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new DuplicateResourceException("Phone number already exists");
        }

        String tempPassword = TemporaryPasswordGenerator.generate();
        User user = User.builder()
                .fullNames(request.getFullNames())
                .email(email)
                .phoneNumber(request.getPhoneNumber())
                .password(passwordEncoder.encode(tempPassword))
                .status(Status.ACTIVE)
                .emailVerified(true)
                .mustChangePassword(true)
                .roles(Set.of(request.getRole()))
                .build();
        userRepository.save(user);

        emailService.sendTemporaryCredentialsEmail(email, user.getFullNames(), tempPassword, request.getRole());
        notificationDispatchService.dispatchToUser(user,
                "Your staff account has been created. Role: " + request.getRole(),
                NotificationType.ROLE_ASSIGNED,
                () -> emailService.sendTemporaryCredentialsEmail(email, user.getFullNames(), tempPassword, request.getRole()));

        auditLogService.log(AuditAction.CREATE, "User", user.getId(),
                "Staff created with role " + request.getRole());

        return EntityMapper.toUserResponse(user);
    }

    @Transactional
    public UserResponse updateRoles(Long id, RoleUpdateRequest request) {
        User user = findById(id);
        Set<RoleName> oldRoles = Set.copyOf(user.getRoles());
        user.setRoles(request.getRoles());
        userRepository.save(user);

        boolean isUpdate = !oldRoles.equals(request.getRoles());
        NotificationType type = isUpdate ? NotificationType.ROLE_UPDATED : NotificationType.ROLE_ASSIGNED;
        notificationDispatchService.dispatchToUser(user,
                "Your roles have been updated to: " + request.getRoles(),
                type,
                () -> emailService.sendRoleAssignmentEmail(user.getEmail(), user.getFullNames(),
                        request.getRoles(), isUpdate));

        auditLogService.log(AuditAction.UPDATE, "User", id, "Roles updated to " + request.getRoles());
        return EntityMapper.toUserResponse(user);
    }

    @Transactional
    public UserResponse updateStatus(Long id, StatusUpdateRequest request) {
        User user = findById(id);
        user.setStatus(request.getStatus());
        userRepository.save(user);
        auditLogService.log(AuditAction.UPDATE, "User", id, "Status updated to " + request.getStatus());
        return EntityMapper.toUserResponse(user);
    }

    public UserResponse getById(Long id) {
        return EntityMapper.toUserResponse(findById(id));
    }

    public PageResponse<UserResponse> getAll(Pageable pageable) {
        return PageResponse.from(userRepository.findAll(pageable).map(EntityMapper::toUserResponse));
    }

    public PageResponse<UserResponse> search(String keyword, Pageable pageable) {
        Page<UserResponse> page = userRepository
                .findByFullNamesContainingIgnoreCaseOrEmailContainingIgnoreCase(keyword, keyword, pageable)
                .map(EntityMapper::toUserResponse);
        return PageResponse.from(page);
    }

    @Transactional
    public UserResponse update(Long id, UserRequest request) {
        User user = findById(id);
        String email = request.getEmail().toLowerCase();
        if (!user.getEmail().equals(email) && userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("Email already exists");
        }
        user.setFullNames(request.getFullNames());
        user.setEmail(email);
        user.setPhoneNumber(request.getPhoneNumber());
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
        }
        if (request.getRoles() != null) {
            user.setRoles(request.getRoles());
        }
        userRepository.save(user);
        auditLogService.log(AuditAction.UPDATE, "User", id, "User updated");
        return EntityMapper.toUserResponse(user);
    }

    @Transactional
    public void delete(Long id) {
        User user = findById(id);
        userRepository.delete(user);
        auditLogService.log(AuditAction.DELETE, "User", id, "User deleted");
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }
}
