package com.zhaobiao.admin.controller;

import com.zhaobiao.admin.common.ApiResponse;
import com.zhaobiao.admin.dto.business.BusinessTypeCreateRequest;
import com.zhaobiao.admin.dto.business.BusinessTypeDto;
import com.zhaobiao.admin.dto.business.BusinessTypeOptionDto;
import com.zhaobiao.admin.dto.business.BusinessTypeStatusUpdateRequest;
import com.zhaobiao.admin.dto.business.BusinessTypeUpdateRequest;
import com.zhaobiao.admin.logging.OperationLogRecord;
import com.zhaobiao.admin.service.BusinessTypeAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@Tag(name = "管理员-业务类型管理")
@RestController
@RequestMapping("/api/admin/business-types")
@PreAuthorize("hasRole('ADMIN')")
public class BusinessTypeAdminController {

    private final BusinessTypeAdminService businessTypeAdminService;

    public BusinessTypeAdminController(BusinessTypeAdminService businessTypeAdminService) {
        this.businessTypeAdminService = businessTypeAdminService;
    }

    @Operation(summary = "查询业务类型列表")
    @PreAuthorize("hasAuthority('SYSTEM_BUSINESS_TYPE')")
    @GetMapping
    public ApiResponse<List<BusinessTypeDto>> list() {
        return ApiResponse.success(businessTypeAdminService.listAll());
    }

    @Operation(summary = "查询启用中的业务类型选项")
    @PreAuthorize("hasAuthority('SYSTEM_BUSINESS_TYPE')")
    @GetMapping("/options")
    public ApiResponse<List<BusinessTypeOptionDto>> options() {
        return ApiResponse.success(businessTypeAdminService.listEnabledOptions());
    }

    @Operation(summary = "新增业务类型")
    @PreAuthorize("hasAuthority('BUSINESS_TYPE_CREATE_BUTTON')")
    @OperationLogRecord(module = "类型管理", action = "新增业务类型")
    @PostMapping
    public ApiResponse<BusinessTypeDto> create(@Valid @RequestBody BusinessTypeCreateRequest request) {
        return ApiResponse.success(businessTypeAdminService.create(request));
    }

    @Operation(summary = "修改业务类型")
    @PreAuthorize("hasAuthority('BUSINESS_TYPE_EDIT_BUTTON')")
    @OperationLogRecord(module = "类型管理", action = "修改业务类型")
    @PutMapping("/{businessTypeId}")
    public ApiResponse<BusinessTypeDto> update(@PathVariable Long businessTypeId,
                                               @Valid @RequestBody BusinessTypeUpdateRequest request) {
        return ApiResponse.success(businessTypeAdminService.update(businessTypeId, request));
    }

    @Operation(summary = "修改业务类型状态")
    @PreAuthorize("hasAuthority('BUSINESS_TYPE_STATUS_BUTTON')")
    @OperationLogRecord(module = "类型管理", action = "修改业务类型状态")
    @PutMapping("/{businessTypeId}/status")
    public ApiResponse<BusinessTypeDto> updateStatus(@PathVariable Long businessTypeId,
                                                     @Valid @RequestBody BusinessTypeStatusUpdateRequest request) {
        return ApiResponse.success(businessTypeAdminService.updateStatus(businessTypeId, request));
    }

    @Operation(summary = "删除业务类型")
    @PreAuthorize("hasAuthority('BUSINESS_TYPE_DELETE_BUTTON')")
    @OperationLogRecord(module = "类型管理", action = "删除业务类型")
    @DeleteMapping("/{businessTypeId}")
    public ApiResponse<Void> delete(@PathVariable Long businessTypeId) {
        businessTypeAdminService.delete(businessTypeId);
        return ApiResponse.success("删除成功", null);
    }
}
