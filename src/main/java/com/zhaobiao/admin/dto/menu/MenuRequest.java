package com.zhaobiao.admin.dto.menu;

import com.zhaobiao.admin.entity.MenuType;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Schema(description = "菜单新增或修改请求")
public class MenuRequest {

    @NotBlank(message = "菜单编码不能为空")
    @Schema(description = "菜单编码", example = "SYSTEM_USER")
    private String code;

    @NotBlank(message = "菜单名称不能为空")
    @Schema(description = "菜单名称", example = "用户管理")
    private String name;

    @NotNull(message = "菜单类型不能为空")
    @Schema(description = "菜单类型", example = "MENU")
    private MenuType type;

    @Schema(description = "父级菜单ID，顶级为空")
    private Long parentId;

    @Size(max = 128, message = "路由路径不能超过 128 位")
    @Schema(description = "前端路由地址", example = "/users")
    private String routePath;

    @Size(max = 128, message = "组件路径不能超过 128 位")
    @Schema(description = "前端组件路径", example = "users/index")
    private String component;

    @Size(max = 64, message = "图标不能超过 64 位")
    @Schema(description = "图标", example = "User")
    private String icon;

    @NotNull(message = "排序不能为空")
    @Schema(description = "排序值", example = "10")
    private Integer sortOrder;

    @NotNull(message = "是否显示不能为空")
    @Schema(description = "是否显示", example = "true")
    private Boolean visible;

    @NotNull(message = "状态不能为空")
    @Schema(description = "是否启用", example = "true")
    private Boolean enabled;

    @Size(max = 64, message = "权限编码不能超过 64 位")
    @Schema(description = "已废弃，后端授权统一使用菜单编码 code", example = "SYSTEM_USER")
    private String permissionCode;

    @Size(max = 255, message = "描述不能超过 255 位")
    @Schema(description = "描述", example = "系统用户管理菜单")
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

    public MenuType getType() {
        return type;
    }

    public void setType(MenuType type) {
        this.type = type;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getRoutePath() {
        return routePath;
    }

    public void setRoutePath(String routePath) {
        this.routePath = routePath;
    }

    public String getComponent() {
        return component;
    }

    public void setComponent(String component) {
        this.component = component;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Boolean getVisible() {
        return visible;
    }

    public void setVisible(Boolean visible) {
        this.visible = visible;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getPermissionCode() {
        return permissionCode;
    }

    public void setPermissionCode(String permissionCode) {
        this.permissionCode = permissionCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
