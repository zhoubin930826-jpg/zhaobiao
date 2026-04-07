package com.zhaobiao.admin.dto.menu;

import com.zhaobiao.admin.entity.MenuType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "菜单信息")
public class MenuDto {

    @Schema(description = "菜单ID")
    private Long id;

    @Schema(description = "菜单编码")
    private String code;

    @Schema(description = "菜单名称")
    private String name;

    @Schema(description = "菜单类型")
    private MenuType type;

    @Schema(description = "父级ID")
    private Long parentId;

    @Schema(description = "前端路由")
    private String routePath;

    @Schema(description = "前端组件")
    private String component;

    @Schema(description = "图标")
    private String icon;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "是否显示")
    private Boolean visible;

    @Schema(description = "是否启用")
    private Boolean enabled;

    @Schema(description = "按钮权限编码")
    private String permissionCode;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "子菜单")
    private List<MenuDto> children;

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

    public List<MenuDto> getChildren() {
        return children;
    }

    public void setChildren(List<MenuDto> children) {
        this.children = children;
    }
}

