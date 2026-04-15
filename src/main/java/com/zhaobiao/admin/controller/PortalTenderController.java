package com.zhaobiao.admin.controller;

import com.zhaobiao.admin.common.ApiResponse;
import com.zhaobiao.admin.common.PageResult;
import com.zhaobiao.admin.dto.tender.TenderDetailDto;
import com.zhaobiao.admin.dto.tender.TenderListItemDto;
import com.zhaobiao.admin.security.MemberLoginUser;
import com.zhaobiao.admin.service.PortalTenderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "门户-招标公告")
@RestController
@RequestMapping("/api/portal/tenders")
public class PortalTenderController {

    private final PortalTenderService portalTenderService;

    public PortalTenderController(PortalTenderService portalTenderService) {
        this.portalTenderService = portalTenderService;
    }

    @Operation(summary = "分页查询门户招标列表")
    @GetMapping
    public ApiResponse<PageResult<TenderListItemDto>> list(@RequestParam(defaultValue = "1") int pageNum,
                                                           @RequestParam(defaultValue = "10") int pageSize,
                                                           @RequestParam(required = false) String keyword,
                                                           @RequestParam(required = false) String region) {
        return ApiResponse.success(portalTenderService.listTenders(pageNum, pageSize, keyword, region));
    }

    @Operation(summary = "查询门户招标详情")
    @GetMapping("/{tenderId}")
    public ApiResponse<TenderDetailDto> detail(@PathVariable Long tenderId) {
        return ApiResponse.success(portalTenderService.getTenderDetail(tenderId));
    }

    @Operation(summary = "下载招标附件")
    @PreAuthorize("hasRole('MEMBER')")
    @GetMapping("/{tenderId}/attachments/{attachmentId}/download")
    public ResponseEntity<Resource> download(@PathVariable Long tenderId,
                                             @PathVariable Long attachmentId,
                                             @AuthenticationPrincipal MemberLoginUser loginUser) {
        return portalTenderService.downloadAttachment(tenderId, attachmentId, loginUser);
    }
}
