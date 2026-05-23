package com.zhaobiao.admin.service;

import com.zhaobiao.admin.common.BusinessException;
import com.zhaobiao.admin.common.PageResult;
import com.zhaobiao.admin.dto.business.BusinessTypeOptionDto;
import com.zhaobiao.admin.dto.tender.TenderAttachmentDto;
import com.zhaobiao.admin.dto.tender.TenderDetailDto;
import com.zhaobiao.admin.dto.tender.TenderListItemDto;
import com.zhaobiao.admin.entity.Tender;
import com.zhaobiao.admin.entity.TenderAttachment;
import com.zhaobiao.admin.entity.TenderFileStorage;
import com.zhaobiao.admin.entity.TenderStatus;
import com.zhaobiao.admin.mapper.ViewMapper;
import com.zhaobiao.admin.repository.TenderAttachmentRepository;
import com.zhaobiao.admin.repository.TenderRepository;
import com.zhaobiao.admin.security.MemberLoginUser;
import com.zhaobiao.admin.util.FileThumbnailUrlBuilder;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PortalTenderService {

    private final TenderRepository tenderRepository;
    private final TenderAttachmentRepository tenderAttachmentRepository;
    private final FileResponseBuilder fileResponseBuilder;
    private final ViewMapper viewMapper;

    public PortalTenderService(TenderRepository tenderRepository,
                               TenderAttachmentRepository tenderAttachmentRepository,
                               FileResponseBuilder fileResponseBuilder,
                               ViewMapper viewMapper) {
        this.tenderRepository = tenderRepository;
        this.tenderAttachmentRepository = tenderAttachmentRepository;
        this.fileResponseBuilder = fileResponseBuilder;
        this.viewMapper = viewMapper;
    }

    @Transactional(readOnly = true)
    public PageResult<TenderListItemDto> listTenders(int pageNum,
                                                     int pageSize,
                                                     String keyword,
                                                     String region,
                                                     String businessTypeName,
                                                     MemberLoginUser loginUser) {
        java.util.List<Long> businessTypeIds = getAccessibleBusinessTypeIds(loginUser);
        Pageable pageable = buildPageable(pageNum, pageSize);
        Page<Tender> page = tenderRepository.searchPortal(
                normalize(keyword),
                normalize(region),
                normalize(businessTypeName),
                TenderStatus.PUBLISHED,
                LocalDateTime.now(),
                businessTypeIds,
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
    public List<TenderListItemDto> listLatestTenders() {
        Pageable pageable = PageRequest.of(0, 3);
        return tenderRepository.findPublicLatest(TenderStatus.PUBLISHED, LocalDateTime.now(), pageable)
                .stream()
                .map(this::toListItemDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TenderDetailDto getTenderDetail(Long tenderId, MemberLoginUser loginUser) {
        Tender tender = tenderRepository.findPublicAccessible(
                        tenderId,
                        TenderStatus.PUBLISHED,
                        LocalDateTime.now())
                .orElseThrow(() -> new BusinessException(404, "招标不存在"));
        List<TenderAttachment> attachments = tenderAttachmentRepository.findDetailsByTenderId(tenderId);
        TenderDetailDto dto = new TenderDetailDto();
        dto.setId(tender.getId());
        dto.setTitle(tender.getTitle());
        dto.setRegion(tender.getRegion());
        dto.setBusinessType(toBusinessTypeDto(tender));
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
        dto.setCanDownload(canDownload(loginUser, tender));
        return dto;
    }

    @Transactional(readOnly = true)
    public ResponseEntity<Resource> downloadAttachment(Long tenderId,
                                                       Long attachmentId,
                                                       MemberLoginUser loginUser) {
        return fileResponseBuilder.download(loadDownloadableAttachment(tenderId, attachmentId, loginUser).getFileStorage());
    }

    private TenderAttachment loadDownloadableAttachment(Long tenderId,
                                                        Long attachmentId,
                                                        MemberLoginUser loginUser) {
        Tender tender = tenderRepository.findPublicAccessible(
                        tenderId,
                        TenderStatus.PUBLISHED,
                        LocalDateTime.now())
                .orElseThrow(() -> new BusinessException(404, "招标不存在"));
        if (!memberCanAccessTender(loginUser, tender)) {
            throw new BusinessException(403, "当前账号暂无附件下载权限");
        }
        if (!loginUser.isCanDownloadFile()) {
            throw new BusinessException(403, "当前账号暂无附件下载权限");
        }
        TenderAttachment attachment = tenderAttachmentRepository.findDetailByIdAndTenderId(attachmentId, tenderId)
                .orElseThrow(() -> new BusinessException(404, "招标附件不存在"));
        return attachment;
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
        dto.setBusinessType(toBusinessTypeDto(tender));
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
        TenderFileStorage fileStorage = attachment.getFileStorage();
        TenderAttachmentDto dto = new TenderAttachmentDto();
        dto.setAttachmentId(attachment.getId());
        dto.setFileId(fileStorage.getId());
        dto.setFileName(fileStorage.getOriginalName());
        dto.setContentType(fileStorage.getContentType());
        dto.setFileSize(fileStorage.getFileSize());
        dto.setThumbnailUrl(FileThumbnailUrlBuilder.build(fileStorage.getId()));
        dto.setThumbnailContentType(fileStorage.getThumbnailContentType());
        dto.setThumbnailStatus(fileStorage.getThumbnailStatus());
        return dto;
    }

    private BusinessTypeOptionDto toBusinessTypeDto(Tender tender) {
        return tender.getBusinessType() == null ? null : viewMapper.toBusinessTypeOptionDto(tender.getBusinessType());
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

    private List<Long> getAccessibleBusinessTypeIds(MemberLoginUser loginUser) {
        if (loginUser == null || loginUser.getBusinessTypeIds() == null || loginUser.getBusinessTypeIds().isEmpty()) {
            throw new BusinessException(403, "账号未分配可用业务类型，请联系管理员");
        }
        return loginUser.getBusinessTypeIds();
    }

    private boolean canDownload(MemberLoginUser loginUser, Tender tender) {
        return loginUser != null && loginUser.isCanDownloadFile() && memberCanAccessTender(loginUser, tender);
    }

    private boolean memberCanAccessTender(MemberLoginUser loginUser, Tender tender) {
        if (loginUser == null || tender == null || tender.getBusinessType() == null) {
            return false;
        }
        return getAccessibleBusinessTypeIds(loginUser).contains(tender.getBusinessType().getId());
    }
}
