package com.zhaobiao.admin.controller;

import com.zhaobiao.admin.common.ApiResponse;
import com.zhaobiao.admin.dto.log.OperationLogDto;
import com.zhaobiao.admin.service.OperationLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "管理员-操作日志")
@RestController
@RequestMapping("/api/admin/operation-logs")
public class OperationLogController {

    private final OperationLogService operationLogService;

    public OperationLogController(OperationLogService operationLogService) {
        this.operationLogService = operationLogService;
    }

    @Operation(summary = "查询操作日志列表")
    @PreAuthorize("hasAuthority('operation:log:view')")
    @GetMapping
    public ApiResponse<List<OperationLogDto>> listLogs() {
        return ApiResponse.success(operationLogService.listLogs());
    }
}
