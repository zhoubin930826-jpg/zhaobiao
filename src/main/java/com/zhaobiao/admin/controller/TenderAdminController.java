package com.zhaobiao.admin.controller;

import com.zhaobiao.admin.common.ApiResponse;
import com.zhaobiao.admin.common.PageResult;
import com.zhaobiao.admin.dto.tender.TenderAttachmentBindRequest;
import com.zhaobiao.admin.dto.tender.TenderDto;
import com.zhaobiao.admin.dto.tender.TenderListItemDto;
import com.zhaobiao.admin.dto.tender.TenderUpsertRequest;
import com.zhaobiao.admin.logging.OperationLogRecord;
import com.zhaobiao.admin.service.TenderService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Tag(name = "管理员-招标管理")
@RestController
@RequestMapping("/api/admin/tenders")
@PreAuthorize("hasRole('ADMIN')")
public class TenderAdminController {

    private final TenderService tenderService;

    public TenderAdminController(TenderService tenderService) {
        this.tenderService = tenderService;
    }

    @Operation(summary = "分页查询招标列表")
    @PreAuthorize("hasAuthority('tender:view')")
    @GetMapping
    public ApiResponse<PageResult<TenderListItemDto>> list(@RequestParam(defaultValue = "1") int pageNum,
                                                           @RequestParam(defaultValue = "10") int pageSize,
                                                           @RequestParam(required = false) String keyword,
                                                           @RequestParam(required = false) String region) {
        return ApiResponse.success(tenderService.listAdminTenders(pageNum, pageSize, keyword, region));
    }

    @Operation(summary = "查询招标详情")
    @PreAuthorize("hasAuthority('tender:view')")
    @GetMapping("/{tenderId}")
    public ApiResponse<TenderDto> detail(@PathVariable Long tenderId) {
        return ApiResponse.success(tenderService.getTender(tenderId));
    }

    @Operation(summary = "新增招标")
    @PreAuthorize("hasAuthority('tender:create')")
    @OperationLogRecord(module = "招标管理", action = "新增招标")
    @PostMapping
    public ApiResponse<TenderDto> create(@Valid @RequestBody TenderUpsertRequest request) {
        return ApiResponse.success(tenderService.createTender(request));
    }

    @Operation(summary = "修改招标")
    @PreAuthorize("hasAuthority('tender:edit')")
    @OperationLogRecord(module = "招标管理", action = "修改招标")
    @PutMapping("/{tenderId}")
    public ApiResponse<TenderDto> update(@PathVariable Long tenderId,
                                         @Valid @RequestBody TenderUpsertRequest request) {
        return ApiResponse.success(tenderService.updateTender(tenderId, request));
    }

    @Operation(summary = "删除招标")
    @PreAuthorize("hasAuthority('tender:delete')")
    @OperationLogRecord(module = "招标管理", action = "删除招标")
    @DeleteMapping("/{tenderId}")
    public ApiResponse<Void> delete(@PathVariable Long tenderId) {
        tenderService.deleteTender(tenderId);
        return ApiResponse.success("删除成功", null);
    }

    @Operation(summary = "为招标追加附件")
    @PreAuthorize("hasAnyAuthority('tender:create', 'tender:edit')")
    @OperationLogRecord(module = "招标管理", action = "追加招标附件")
    @PostMapping("/{tenderId}/attachments")
    public ApiResponse<TenderDto> addAttachments(@PathVariable Long tenderId,
                                                 @Valid @RequestBody TenderAttachmentBindRequest request) {
        return ApiResponse.success(tenderService.addAttachments(tenderId, request.getFileIds()));
    }

    @Operation(summary = "删除招标附件")
    @PreAuthorize("hasAuthority('tender:edit')")
    @OperationLogRecord(module = "招标管理", action = "删除招标附件")
    @DeleteMapping("/{tenderId}/attachments/{attachmentId}")
    public ApiResponse<TenderDto> removeAttachment(@PathVariable Long tenderId,
                                                   @PathVariable Long attachmentId) {
        return ApiResponse.success(tenderService.removeAttachment(tenderId, attachmentId));
    }
}
