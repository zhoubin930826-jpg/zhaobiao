package com.zhaobiao.admin.dto.permission;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotBlank;

@Schema(description = "权限新增或修改请求")
public class PermissionRequest {

    @NotBlank(message = "权限编码不能为空")
    @Schema(description = "权限编码", example = "user:view")
    private String code;

    @NotBlank(message = "权限名称不能为空")
    @Schema(description = "权限名称", example = "查看用户")
    private String name;

    @Schema(description = "权限描述", example = "用于查看用户列表")
    private String description;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

