package rw.gov.utility_billing_system.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import rw.gov.utility_billing_system.entity.User;
import rw.gov.utility_billing_system.enums.Status;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.stream.Collectors;

@Getter
public class UserPrincipal implements UserDetails {

    private final Long id;
    private final String email;
    private final String password;
    private final String fullNames;
    private final Status status;
    private final boolean accountLocked;
    private final LocalDateTime accountLockedUntil;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.password = user.getPassword();
        this.fullNames = user.getFullNames();
        this.status = user.getStatus();
        this.accountLocked = user.isAccountLocked();
        this.accountLockedUntil = user.getAccountLockedUntil();
        this.authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.name()))
                .collect(Collectors.toSet());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        if (status != Status.ACTIVE) {
            return false;
        }
        if (!accountLocked) {
            return true;
        }
        return accountLockedUntil != null && accountLockedUntil.isBefore(LocalDateTime.now());
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return status == Status.ACTIVE && email != null;
    }
}
