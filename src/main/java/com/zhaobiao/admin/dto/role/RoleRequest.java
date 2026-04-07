package com.zhaobiao.admin.dto.role;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Schema(description = "角色新增或修改请求")
public class RoleRequest {

    @NotBlank(message = "角色编码不能为空")
    @Schema(description = "角色编码", example = "BID_MANAGER")
    private String code;

    @NotBlank(message = "角色名称不能为空")
    @Schema(description = "角色名称", example = "招标管理员")
    private String name;

    @Schema(description = "角色描述")
    private String description;

    @NotEmpty(message = "请至少分配一个权限")
    @Schema(description = "权限ID列表")
    private List<Long> permissionIds;

    @NotEmpty(message = "请至少分配一个菜单")
    @Schema(description = "菜单ID列表")
    private List<Long> menuIds;

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

    public List<Long> getPermissionIds() {
        return permissionIds;
    }

    public void setPermissionIds(List<Long> permissionIds) {
        this.permissionIds = permissionIds;
    }

    public List<Long> getMenuIds() {
        return menuIds;
    }

    public void setMenuIds(List<Long> menuIds) {
        this.menuIds = menuIds;
    }
}
