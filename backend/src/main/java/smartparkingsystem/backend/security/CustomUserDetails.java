package smartparkingsystem.backend.security;

import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import smartparkingsystem.backend.entity.User;
import smartparkingsystem.backend.entity.type.UserRole;
import smartparkingsystem.backend.entity.type.UserStatus;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

@AllArgsConstructor
public class CustomUserDetails implements UserDetails {
    private UUID id;
    private String username;
    private String password;
    private UserRole role;
    private boolean enabled;

    public static CustomUserDetails build(User user) {
        return new CustomUserDetails(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                user.getRole(),
                user.getStatus() == UserStatus.ACTIVE
        );
    }

    public UUID getId() {
        return id;
    }

    public UserRole getRole() {
        return role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + role.name())
        );
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}

