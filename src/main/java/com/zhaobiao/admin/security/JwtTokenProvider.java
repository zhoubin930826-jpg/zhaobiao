package com.zhaobiao.admin.security;

import com.zhaobiao.admin.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.List;

@Component
public class JwtTokenProvider {

    private final JwtProperties jwtProperties;

    private Key key;

    public JwtTokenProvider(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(LoginUser loginUser) {
        return generateToken(
                loginUser.getUsername(),
                loginUser.getUserId(),
                TokenUserType.ADMIN,
                loginUser.getRoleCodes()
        );
    }

    public String generateToken(MemberLoginUser loginUser) {
        return generateToken(
                loginUser.getUsername(),
                loginUser.getUserId(),
                TokenUserType.MEMBER,
                null
        );
    }

    private String generateToken(String username,
                                 Long userId,
                                 TokenUserType userType,
                                 List<String> roleCodes) {
        long now = System.currentTimeMillis();
        long expireMillis = jwtProperties.getExpireSeconds() * 1000;
        JwtBuilder builder = Jwts.builder()
                .setSubject(username)
                .claim("userId", userId)
                .claim("userType", userType.name())
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + expireMillis))
                .signWith(key, SignatureAlgorithm.HS256);
        if (roleCodes != null && !roleCodes.isEmpty()) {
            builder.claim("roleCodes", roleCodes);
        }
        return builder.compact();
    }

    public String getUsername(String token) {
        return parseClaims(token).getSubject();
    }

    public TokenUserType getUserType(String token) {
        String userType = parseClaims(token).get("userType", String.class);
        if (userType == null) {
            return TokenUserType.ADMIN;
        }
        return TokenUserType.valueOf(userType);
    }

    public boolean validate(String token) {
        try {
            Claims claims = parseClaims(token);
            return claims.getExpiration().after(new Date());
        } catch (Exception ex) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
