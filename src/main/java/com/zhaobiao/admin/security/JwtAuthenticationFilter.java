package com.zhaobiao.admin.security;

import com.zhaobiao.admin.common.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService adminUserDetailsService;
    private final MemberUserDetailsService memberUserDetailsService;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider,
                                   CustomUserDetailsService adminUserDetailsService,
                                   MemberUserDetailsService memberUserDetailsService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.adminUserDetailsService = adminUserDetailsService;
        this.memberUserDetailsService = memberUserDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = resolveToken(request);
        if (StringUtils.hasText(token) && jwtTokenProvider.validate(token)
                && SecurityContextHolder.getContext().getAuthentication() == null) {
            String username = jwtTokenProvider.getUsername(token);
            TokenUserType userType = jwtTokenProvider.getUserType(token);
            UserDetails userDetails;
            try {
                userDetails = userType == TokenUserType.MEMBER
                        ? memberUserDetailsService.loadUserByUsername(username)
                        : adminUserDetailsService.loadUserByUsername(username);
            } catch (BusinessException ex) {
                LOGGER.debug("JWT 对应用户不可用: userType={}, username={}, uri={}, reason={}",
                        userType, username, request.getRequestURI(), ex.getMessage());
                filterChain.doFilter(request, response);
                return;
            }
            if (!userDetails.isEnabled()
                    || !userDetails.isAccountNonLocked()
                    || !userDetails.isAccountNonExpired()
                    || !userDetails.isCredentialsNonExpired()) {
                LOGGER.debug("JWT 对应账号状态不可用: userType={}, username={}, uri={}",
                        userType, username, request.getRequestURI());
                filterChain.doFilter(request, response);
                return;
            }
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (StringUtils.hasText(authorization) && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        return null;
    }
}
