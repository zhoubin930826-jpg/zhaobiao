package com.zhaobiao.admin.dto.business;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Schema(description = "业务类型修改请求")
public class BusinessTypeUpdateRequest {

    @NotBlank(message = "类型编码不能为空")
    @Size(max = 64, message = "类型编码不能超过 64 位")
    @Schema(description = "类型编码", example = "ENGINEERING")
    private String code;

    @NotBlank(message = "类型名称不能为空")
    @Size(max = 64, message = "类型名称不能超过 64 位")
    @Schema(description = "类型名称", example = "工程")
    private String name;

    @NotNull(message = "启用状态不能为空")
    @Schema(description = "是否启用", example = "true")
    private Boolean enabled;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Size(max = 255, message = "描述不能超过 255 位")
    @Schema(description = "描述")
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

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
