package rw.gov.utility_billing_system.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.gov.utility_billing_system.dto.request.customer.CustomerRequest;
import rw.gov.utility_billing_system.dto.request.customer.CustomerSelfRegisterRequest;
import rw.gov.utility_billing_system.dto.response.PageResponse;
import rw.gov.utility_billing_system.dto.response.customer.CustomerResponse;
import rw.gov.utility_billing_system.entity.Customer;
import rw.gov.utility_billing_system.entity.User;
import rw.gov.utility_billing_system.enums.AuditAction;
import rw.gov.utility_billing_system.enums.NotificationType;
import rw.gov.utility_billing_system.enums.OtpType;
import rw.gov.utility_billing_system.enums.RegistrationSource;
import rw.gov.utility_billing_system.enums.RoleName;
import rw.gov.utility_billing_system.enums.Status;
import rw.gov.utility_billing_system.exception.DuplicateResourceException;
import rw.gov.utility_billing_system.exception.ResourceNotFoundException;
import rw.gov.utility_billing_system.mapper.EntityMapper;
import rw.gov.utility_billing_system.repository.CustomerRepository;
import rw.gov.utility_billing_system.repository.UserRepository;
import rw.gov.utility_billing_system.utility.EmailService;
import rw.gov.utility_billing_system.utility.TemporaryPasswordGenerator;

import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;
    private final EmailService emailService;
    private final OtpService otpService;
    private final NotificationDispatchService notificationDispatchService;

    @Transactional
    public CustomerResponse selfRegister(CustomerSelfRegisterRequest request) {
        validateDuplicates(request.getNationalId(), request.getEmail(), null);
        String email = request.getEmail().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("Email already registered");
        }
        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new DuplicateResourceException("Phone number already registered");
        }

        User user = User.builder()
                .fullNames(request.getFullNames())
                .email(email)
                .phoneNumber(request.getPhoneNumber())
                .password(passwordEncoder.encode(request.getPassword()))
                .status(Status.INACTIVE)
                .emailVerified(false)
                .emailVerificationToken(UUID.randomUUID().toString())
                .roles(Set.of(RoleName.ROLE_CUSTOMER))
                .build();
        userRepository.save(user);

        Customer customer = Customer.builder()
                .fullNames(request.getFullNames())
                .nationalId(request.getNationalId())
                .email(email)
                .phoneNumber(request.getPhoneNumber())
                .address(request.getAddress())
                .status(Status.INACTIVE)
                .registrationSource(RegistrationSource.SELF_REGISTERED)
                .accountVerified(false)
                .user(user)
                .build();
        user.setCustomer(customer);
        customerRepository.save(customer);

        String otp = otpService.generateOtp(email, OtpType.REGISTRATION);
        emailService.sendOtpEmail(email, otp);

        auditLogService.log(AuditAction.CREATE, "Customer", customer.getId(), "Customer self-registered");

        return EntityMapper.toCustomerResponse(customer);
    }

    @Transactional
    public CustomerResponse create(CustomerRequest request) {
        validateDuplicates(request.getNationalId(), request.getEmail(), null);
        String email = request.getEmail().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("Email already registered");
        }
        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new DuplicateResourceException("Phone number already registered");
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
                .roles(Set.of(RoleName.ROLE_CUSTOMER))
                .build();
        userRepository.save(user);

        Customer customer = Customer.builder()
                .fullNames(request.getFullNames())
                .nationalId(request.getNationalId())
                .email(email)
                .phoneNumber(request.getPhoneNumber())
                .address(request.getAddress())
                .status(request.getStatus() != null ? request.getStatus() : Status.ACTIVE)
                .registrationSource(RegistrationSource.ADMIN_CREATED)
                .accountVerified(true)
                .user(user)
                .build();
        user.setCustomer(customer);
        customerRepository.save(customer);

        emailService.sendCustomerCredentialsEmail(email, customer.getFullNames(), tempPassword);
        notificationDispatchService.dispatchToCustomer(customer,
                "Your customer account has been created. Please check your email for login credentials.",
                NotificationType.GENERAL,
                () -> emailService.sendCustomerCredentialsEmail(email, customer.getFullNames(), tempPassword));

        auditLogService.log(AuditAction.CREATE, "Customer", customer.getId(), "Customer created by admin");
        return EntityMapper.toCustomerResponse(customer);
    }

    public CustomerResponse getById(Long id) {
        return EntityMapper.toCustomerResponse(findById(id));
    }

    public PageResponse<CustomerResponse> getAll(Pageable pageable) {
        return PageResponse.from(customerRepository.findAll(pageable).map(EntityMapper::toCustomerResponse));
    }

    public PageResponse<CustomerResponse> search(String keyword, Pageable pageable) {
        Page<CustomerResponse> page = customerRepository
                .findByFullNamesContainingIgnoreCaseOrNationalIdContainingIgnoreCaseOrEmailContainingIgnoreCase(
                        keyword, keyword, keyword, pageable)
                .map(EntityMapper::toCustomerResponse);
        return PageResponse.from(page);
    }

    @Transactional
    public CustomerResponse update(Long id, CustomerRequest request) {
        Customer customer = findById(id);
        validateDuplicates(request.getNationalId(), request.getEmail(), id);
        customer.setFullNames(request.getFullNames());
        customer.setNationalId(request.getNationalId());
        customer.setEmail(request.getEmail().toLowerCase());
        customer.setPhoneNumber(request.getPhoneNumber());
        customer.setAddress(request.getAddress());
        if (request.getStatus() != null) {
            customer.setStatus(request.getStatus());
        }
        customerRepository.save(customer);
        auditLogService.log(AuditAction.UPDATE, "Customer", id, "Customer updated");
        return EntityMapper.toCustomerResponse(customer);
    }

    @Transactional
    public void delete(Long id) {
        Customer customer = findById(id);
        customerRepository.delete(customer);
        auditLogService.log(AuditAction.DELETE, "Customer", id, "Customer deleted");
    }

    @Transactional
    public CustomerResponse verifySelfRegisteredCustomer(String email) {
        Customer customer = customerRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        customer.setAccountVerified(true);
        customer.setStatus(Status.ACTIVE);
        if (customer.getUser() != null) {
            customer.getUser().setStatus(Status.ACTIVE);
            customer.getUser().setEmailVerified(true);
        }
        customerRepository.save(customer);
        return EntityMapper.toCustomerResponse(customer);
    }

    public Customer findById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + id));
    }

    private void validateDuplicates(String nationalId, String email, Long excludeId) {
        customerRepository.findByNationalId(nationalId).ifPresent(c -> {
            if (excludeId == null || !c.getId().equals(excludeId)) {
                throw new DuplicateResourceException("National ID already registered");
            }
        });
        customerRepository.findByEmail(email.toLowerCase()).ifPresent(c -> {
            if (excludeId == null || !c.getId().equals(excludeId)) {
                throw new DuplicateResourceException("Email already registered for a customer");
            }
        });
    }
}
