package com.zhaobiao.admin.security;

import com.zhaobiao.admin.common.BusinessException;
import com.zhaobiao.admin.repository.MemberUserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MemberUserDetailsService implements UserDetailsService {

    private final MemberUserRepository memberUserRepository;

    public MemberUserDetailsService(MemberUserRepository memberUserRepository) {
        this.memberUserRepository = memberUserRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) {
        return memberUserRepository.findDetailByUsername(username)
                .map(MemberLoginUser::from)
                .orElseThrow(() -> new BusinessException(404, "会员不存在"));
    }
}
