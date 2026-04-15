package com.zhaobiao.admin.dto.member;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "会员登录响应")
public class MemberLoginResponse {

    @Schema(description = "访问令牌")
    private String token;

    @Schema(description = "令牌类型")
    private String tokenType;

    @Schema(description = "过期秒数")
    private long expireSeconds;

    @Schema(description = "会员信息")
    private MemberUserDto user;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public long getExpireSeconds() {
        return expireSeconds;
    }

    public void setExpireSeconds(long expireSeconds) {
        this.expireSeconds = expireSeconds;
    }

    public MemberUserDto getUser() {
        return user;
    }

    public void setUser(MemberUserDto user) {
        this.user = user;
    }
}

