package com.zhaobiao.admin.security;

import com.zhaobiao.admin.entity.MemberUser;
import com.zhaobiao.admin.entity.MemberUserStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class MemberLoginUser implements UserDetails {

    private final Long userId;
    private final String username;
    private final String password;
    private final MemberUserStatus status;
    private final List<GrantedAuthority> authorities;

    public MemberLoginUser(Long userId,
                           String username,
                           String password,
                           MemberUserStatus status,
                           List<GrantedAuthority> authorities) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.status = status;
        this.authorities = authorities;
    }

    public static MemberLoginUser from(MemberUser user) {
        return new MemberLoginUser(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                user.getStatus(),
                Arrays.asList(new SimpleGrantedAuthority("ROLE_MEMBER"))
        );
    }

    public Long getUserId() {
        return userId;
    }

    public MemberUserStatus getStatus() {
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
        return status != MemberUserStatus.DISABLED;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return status == MemberUserStatus.ENABLED;
    }
}

