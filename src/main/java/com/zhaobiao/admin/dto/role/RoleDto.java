package com.zhaobiao.admin.dto.role;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "角色信息")
public class RoleDto {

    @Schema(description = "角色ID")
    private Long id;

    @Schema(description = "角色编码")
    private String code;

    @Schema(description = "角色名称")
    private String name;

    @Schema(description = "角色描述")
    private String description;

    @Schema(description = "是否系统内置")
    private boolean builtIn;

    @Schema(description = "已废弃的兼容字段，固定返回空列表")
    private List<Long> permissionIds;

    @Schema(description = "已废弃的兼容字段，固定返回空列表；授权编码请读取 menuCodes")
    private List<String> permissionCodes;

    @Schema(description = "菜单ID列表")
    private List<Long> menuIds;

    @Schema(description = "菜单编码列表，后端实际授权编码来源")
    private List<String> menuCodes;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public boolean isBuiltIn() {
        return builtIn;
    }

    public void setBuiltIn(boolean builtIn) {
        this.builtIn = builtIn;
    }

    public List<Long> getPermissionIds() {
        return permissionIds;
    }

    public void setPermissionIds(List<Long> permissionIds) {
        this.permissionIds = permissionIds;
    }

    public List<String> getPermissionCodes() {
        return permissionCodes;
    }

    public void setPermissionCodes(List<String> permissionCodes) {
        this.permissionCodes = permissionCodes;
    }

    public List<Long> getMenuIds() {
        return menuIds;
    }

    public void setMenuIds(List<Long> menuIds) {
        this.menuIds = menuIds;
    }

    public List<String> getMenuCodes() {
        return menuCodes;
    }

    public void setMenuCodes(List<String> menuCodes) {
        this.menuCodes = menuCodes;
    }
}
