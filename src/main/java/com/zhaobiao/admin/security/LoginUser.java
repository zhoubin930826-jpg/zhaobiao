package com.zhaobiao.admin.security;

import com.zhaobiao.admin.entity.Menu;
import com.zhaobiao.admin.entity.Role;
import com.zhaobiao.admin.entity.User;
import com.zhaobiao.admin.entity.UserStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LoginUser implements UserDetails {

    private final Long userId;
    private final String username;
    private final String password;
    private final UserStatus status;
    private final List<Long> roleIds;
    private final List<String> roleCodes;
    private final List<GrantedAuthority> authorities;

    public LoginUser(Long userId,
                     String username,
                     String password,
                     UserStatus status,
                     List<Long> roleIds,
                     List<String> roleCodes,
                     List<GrantedAuthority> authorities) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.status = status;
        this.roleIds = roleIds;
        this.roleCodes = roleCodes;
        this.authorities = authorities;
    }

    public static LoginUser from(User user) {
        List<String> roleCodes = user.getRoles().stream()
                .map(Role::getCode)
                .sorted()
                .collect(Collectors.toList());
        List<Long> roleIds = user.getRoles().stream()
                .map(Role::getId)
                .sorted()
                .collect(Collectors.toList());
        List<GrantedAuthority> authorities = Stream.concat(
                        Stream.concat(
                                Stream.of(new SimpleGrantedAuthority("ROLE_ADMIN")),
                                user.getRoles().stream().map(role -> new SimpleGrantedAuthority("ROLE_" + role.getCode()))
                        ),
                        user.getRoles().stream()
                                .map(Role::getMenus)
                                .flatMap(Collection::stream)
                                .filter(Menu::isEnabled)
                                .map(Menu::getCode)
                                .distinct()
                                .map(SimpleGrantedAuthority::new)
                )
                .collect(Collectors.toList());

        return new LoginUser(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                user.getStatus(),
                roleIds,
                roleCodes,
                authorities
        );
    }

    public Long getUserId() {
        return userId;
    }

    public List<Long> getRoleIds() {
        return roleIds;
    }

    public List<String> getRoleCodes() {
        return roleCodes;
    }

    public boolean hasRole(String roleCode) {
        return roleCodes.contains(roleCode);
    }

    public UserStatus getStatus() {
        return status;
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
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return status != UserStatus.DISABLED;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return status == UserStatus.APPROVED;
    }
}
