package com.zhaobiao.admin.security;

import com.zhaobiao.admin.entity.MemberUser;
import com.zhaobiao.admin.entity.MemberUserStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class MemberLoginUser implements UserDetails {

    private final Long userId;
    private final String username;
    private final String password;
    private final MemberUserStatus status;
    private final LocalDateTime expiresAt;
    private final boolean canDownloadFile;
    private final List<Long> businessTypeIds;
    private final List<GrantedAuthority> authorities;

    public MemberLoginUser(Long userId,
                           String username,
                           String password,
                           MemberUserStatus status,
                           LocalDateTime expiresAt,
                           boolean canDownloadFile,
                           List<Long> businessTypeIds,
                           List<GrantedAuthority> authorities) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.status = status;
        this.expiresAt = expiresAt;
        this.canDownloadFile = canDownloadFile;
        this.businessTypeIds = businessTypeIds;
        this.authorities = authorities;
    }

    public static MemberLoginUser from(MemberUser user) {
        return new MemberLoginUser(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                user.getStatus(),
                user.getExpiresAt(),
                user.isCanDownloadFile(),
                user.getBusinessTypes().stream()
                        .filter(item -> item != null && item.isEnabled())
                        .map(item -> item.getId())
                        .sorted()
                        .collect(Collectors.toList()),
                Arrays.asList(new SimpleGrantedAuthority("ROLE_MEMBER"))
        );
    }

    public Long getUserId() {
        return userId;
    }

    public MemberUserStatus getStatus() {
        return status;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public boolean isCanDownloadFile() {
        return canDownloadFile;
    }

    public List<Long> getBusinessTypeIds() {
        return businessTypeIds;
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
        return expiresAt != null && expiresAt.isAfter(LocalDateTime.now());
    }

    @Override
    public boolean isAccountNonLocked() {
        return status != MemberUserStatus.DISABLED;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return status == MemberUserStatus.ENABLED && !businessTypeIds.isEmpty();
    }
}
