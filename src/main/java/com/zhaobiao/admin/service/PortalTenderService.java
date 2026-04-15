package com.zhaobiao.admin.service;

import com.zhaobiao.admin.common.BusinessException;
import com.zhaobiao.admin.common.PageResult;
import com.zhaobiao.admin.dto.tender.TenderAttachmentDto;
import com.zhaobiao.admin.dto.tender.TenderDetailDto;
import com.zhaobiao.admin.dto.tender.TenderListItemDto;
import com.zhaobiao.admin.entity.MemberUser;
import com.zhaobiao.admin.entity.MemberUserStatus;
import com.zhaobiao.admin.entity.Tender;
import com.zhaobiao.admin.entity.TenderAttachment;
import com.zhaobiao.admin.entity.TenderFileStorage;
import com.zhaobiao.admin.entity.TenderStatus;
import com.zhaobiao.admin.repository.MemberUserRepository;
import com.zhaobiao.admin.repository.TenderAttachmentRepository;
import com.zhaobiao.admin.repository.TenderRepository;
import com.zhaobiao.admin.security.MemberLoginUser;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PortalTenderService {

    private final TenderRepository tenderRepository;
    private final TenderAttachmentRepository tenderAttachmentRepository;
    private final MemberUserRepository memberUserRepository;
    private final LocalFileStorageService localFileStorageService;

    public PortalTenderService(TenderRepository tenderRepository,
                               TenderAttachmentRepository tenderAttachmentRepository,
                               MemberUserRepository memberUserRepository,
                               LocalFileStorageService localFileStorageService) {
        this.tenderRepository = tenderRepository;
        this.tenderAttachmentRepository = tenderAttachmentRepository;
        this.memberUserRepository = memberUserRepository;
        this.localFileStorageService = localFileStorageService;
    }

    @Transactional(readOnly = true)
    public PageResult<TenderListItemDto> listTenders(int pageNum, int pageSize, String keyword, String region) {
        Pageable pageable = buildPageable(pageNum, pageSize);
        Page<Tender> page = tenderRepository.searchPortal(
                normalize(keyword),
                normalize(region),
                TenderStatus.PUBLISHED,
                LocalDateTime.now(),
                pageable
        );
        PageResult<TenderListItemDto> result = new PageResult<>();
        result.setPageNum(pageNum <= 0 ? 1 : pageNum);
        result.setPageSize(pageSize <= 0 ? 10 : Math.min(pageSize, 50));
        result.setTotal(page.getTotalElements());
        result.setTotalPages(page.getTotalPages());
        result.setList(page.getContent().stream().map(this::toListItemDto).collect(Collectors.toList()));
        return result;
    }

    @Transactional(readOnly = true)
    public TenderDetailDto getTenderDetail(Long tenderId) {
        Tender tender = tenderRepository.findByIdAndStatusAndPublishAtLessThanEqual(
                        tenderId,
                        TenderStatus.PUBLISHED,
                        LocalDateTime.now()
                )
                .orElseThrow(() -> new BusinessException(404, "招标不存在"));
        List<TenderAttachment> attachments = tenderAttachmentRepository.findDetailsByTenderId(tenderId);
        TenderDetailDto dto = new TenderDetailDto();
        dto.setId(tender.getId());
        dto.setTitle(tender.getTitle());
        dto.setRegion(tender.getRegion());
        dto.setPublishAt(tender.getPublishAt());
        dto.setContent(tender.getContent());
        dto.setContactPerson(tender.getContactPerson());
        dto.setBudget(tender.getBudget());
        dto.setContactPhone(tender.getContactPhone());
        dto.setTenderUnit(tender.getTenderUnit());
        dto.setDeadline(tender.getDeadline());
        dto.setProjectCode(tender.getProjectCode());
        dto.setSignupDeadline(tender.getSignupDeadline());
        dto.setStatus(tender.getStatus());
        dto.setSummary(extractSummary(tender.getContent()));
        dto.setAttachments(attachments.stream().map(this::toAttachmentDto).collect(Collectors.toList()));
        dto.setCanDownload(currentMemberCanDownload());
        return dto;
    }

    @Transactional(readOnly = true)
    public ResponseEntity<Resource> downloadAttachment(Long tenderId,
                                                       Long attachmentId,
                                                       MemberLoginUser loginUser) {
        MemberUser memberUser = memberUserRepository.findById(loginUser.getUserId())
                .orElseThrow(() -> new BusinessException(404, "会员不存在"));
        if (memberUser.getStatus() != MemberUserStatus.ENABLED) {
            throw new BusinessException(403, "账号已被禁用");
        }
        if (!memberUser.isCanDownloadFile()) {
            throw new BusinessException(403, "当前账号暂无附件下载权限");
        }
        tenderRepository.findByIdAndStatusAndPublishAtLessThanEqual(
                        tenderId,
                        TenderStatus.PUBLISHED,
                        LocalDateTime.now()
                )
                .orElseThrow(() -> new BusinessException(404, "招标不存在"));
        TenderAttachment attachment = tenderAttachmentRepository.findDetailByIdAndTenderId(attachmentId, tenderId)
                .orElseThrow(() -> new BusinessException(404, "招标附件不存在"));
        TenderFileStorage fileStorage = attachment.getFileStorage();
        Resource resource = localFileStorageService.loadAsResource(fileStorage);
        return ResponseEntity.ok()
                .contentType(resolveMediaType(fileStorage.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, buildAttachmentDisposition(fileStorage.getOriginalName()))
                .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(fileStorage.getFileSize()))
                .body(resource);
    }

    private Pageable buildPageable(int pageNum, int pageSize) {
        int safePageNum = pageNum <= 0 ? 1 : pageNum;
        int safePageSize = pageSize <= 0 ? 10 : Math.min(pageSize, 50);
        return PageRequest.of(safePageNum - 1, safePageSize, Sort.by(Sort.Order.desc("publishAt"), Sort.Order.desc("id")));
    }

    private String normalize(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private TenderListItemDto toListItemDto(Tender tender) {
        TenderListItemDto dto = new TenderListItemDto();
        dto.setId(tender.getId());
        dto.setTitle(tender.getTitle());
        dto.setRegion(tender.getRegion());
        dto.setTenderUnit(tender.getTenderUnit());
        dto.setBudget(tender.getBudget());
        dto.setProjectCode(tender.getProjectCode());
        dto.setPublishAt(tender.getPublishAt());
        dto.setDeadline(tender.getDeadline());
        dto.setStatus(tender.getStatus());
        dto.setSummary(extractSummary(tender.getContent()));
        return dto;
    }

    private TenderAttachmentDto toAttachmentDto(TenderAttachment attachment) {
        TenderAttachmentDto dto = new TenderAttachmentDto();
        dto.setAttachmentId(attachment.getId());
        dto.setFileId(attachment.getFileStorage().getId());
        dto.setFileName(attachment.getFileStorage().getOriginalName());
        dto.setContentType(attachment.getFileStorage().getContentType());
        dto.setFileSize(attachment.getFileStorage().getFileSize());
        return dto;
    }

    private boolean currentMemberCanDownload() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof MemberLoginUser)) {
            return false;
        }
        Long memberId = ((MemberLoginUser) authentication.getPrincipal()).getUserId();
        return memberUserRepository.findById(memberId)
                .map(item -> item.getStatus() == MemberUserStatus.ENABLED && item.isCanDownloadFile())
                .orElse(false);
    }

    private MediaType resolveMediaType(String contentType) {
        if (!StringUtils.hasText(contentType)) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
        try {
            return MediaType.parseMediaType(contentType);
        } catch (Exception ex) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }

    private String buildAttachmentDisposition(String fileName) {
        try {
            String encoded = URLEncoder.encode(fileName, StandardCharsets.UTF_8.name()).replace("+", "%20");
            return "attachment; filename*=UTF-8''" + encoded;
        } catch (UnsupportedEncodingException ex) {
            return "attachment";
        }
    }

    private String extractSummary(String content) {
        if (!StringUtils.hasText(content)) {
            return "";
        }
        String plainText = content.replaceAll("<[^>]+>", " ")
                .replaceAll("\\s+", " ")
                .trim();
        if (plainText.length() <= 120) {
            return plainText;
        }
        return plainText.substring(0, 120) + "...";
    }
}
