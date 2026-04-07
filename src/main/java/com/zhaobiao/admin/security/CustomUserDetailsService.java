package com.zhaobiao.admin.security;

import com.zhaobiao.admin.common.BusinessException;
import com.zhaobiao.admin.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) {
        return userRepository.findDetailByUsername(username)
                .map(LoginUser::from)
                .orElseThrow(() -> new BusinessException(404, "用户不存在"));
    }
}
