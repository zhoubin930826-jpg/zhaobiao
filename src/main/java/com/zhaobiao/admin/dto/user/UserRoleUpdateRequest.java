package com.zhaobiao.admin.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@Schema(description = "修改用户角色请求")
public class UserRoleUpdateRequest {

    @NotEmpty(message = "至少选择一个角色")
    @Schema(description = "角色ID列表")
    private List<Long> roleIds;

    public List<Long> getRoleIds() {
        return roleIds;
    }

    public void setRoleIds(List<Long> roleIds) {
        this.roleIds = roleIds;
    }
}
