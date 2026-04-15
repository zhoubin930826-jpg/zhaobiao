package com.zhaobiao.admin.service;

import com.zhaobiao.admin.common.BusinessException;
import com.zhaobiao.admin.common.PageResult;
import com.zhaobiao.admin.dto.tender.TenderAttachmentDto;
import com.zhaobiao.admin.dto.tender.TenderDto;
import com.zhaobiao.admin.dto.tender.TenderListItemDto;
import com.zhaobiao.admin.dto.tender.TenderUpsertRequest;
import com.zhaobiao.admin.entity.Tender;
import com.zhaobiao.admin.entity.TenderAttachment;
import com.zhaobiao.admin.entity.TenderFileStorage;
import com.zhaobiao.admin.entity.TenderStatus;
import com.zhaobiao.admin.repository.TenderAttachmentRepository;
import com.zhaobiao.admin.repository.TenderFileStorageRepository;
import com.zhaobiao.admin.repository.TenderRepository;
import com.zhaobiao.admin.security.LoginUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TenderService {

    private final TenderRepository tenderRepository;
    private final TenderAttachmentRepository tenderAttachmentRepository;
    private final TenderFileStorageRepository tenderFileStorageRepository;
    private final LocalFileStorageService localFileStorageService;

    public TenderService(TenderRepository tenderRepository,
                         TenderAttachmentRepository tenderAttachmentRepository,
                         TenderFileStorageRepository tenderFileStorageRepository,
                         LocalFileStorageService localFileStorageService) {
        this.tenderRepository = tenderRepository;
        this.tenderAttachmentRepository = tenderAttachmentRepository;
        this.tenderFileStorageRepository = tenderFileStorageRepository;
        this.localFileStorageService = localFileStorageService;
    }

    @Transactional(readOnly = true)
    public PageResult<TenderListItemDto> listAdminTenders(int pageNum, int pageSize, String keyword, String region) {
        Pageable pageable = buildPageable(pageNum, pageSize);
        Page<Tender> page = tenderRepository.searchAdmin(normalize(keyword), normalize(region), pageable);
        return toPageResult(page, pageNum, pageSize);
    }

    @Transactional(readOnly = true)
    public TenderDto getTender(Long tenderId) {
        Tender tender = tenderRepository.findById(tenderId)
                .orElseThrow(() -> new BusinessException(404, "招标不存在"));
        return toAdminDto(tender, tenderAttachmentRepository.findDetailsByTenderId(tenderId));
    }

    @Transactional
    public TenderDto createTender(TenderUpsertRequest request) {
        validateRequest(request, null);
        Tender tender = new Tender();
        applyRequest(tender, request, true);
        tender = tenderRepository.save(tender);
        if (request.getAttachmentFileIds() != null) {
            syncAttachments(tender, request.getAttachmentFileIds());
        }
        return getTender(tender.getId());
    }

    @Transactional
    public TenderDto updateTender(Long tenderId, TenderUpsertRequest request) {
        Tender tender = tenderRepository.findById(tenderId)
                .orElseThrow(() -> new BusinessException(404, "招标不存在"));
        validateRequest(request, tenderId);
        applyRequest(tender, request, false);
        tender = tenderRepository.save(tender);
        if (request.getAttachmentFileIds() != null) {
            syncAttachments(tender, request.getAttachmentFileIds());
        }
        return getTender(tender.getId());
    }

    @Transactional
    public void deleteTender(Long tenderId) {
        Tender tender = tenderRepository.findById(tenderId)
                .orElseThrow(() -> new BusinessException(404, "招标不存在"));
        List<TenderAttachment> attachments = tenderAttachmentRepository.findDetailsByTenderId(tenderId);
        tenderAttachmentRepository.deleteAll(attachments);
        cleanupUnreferencedFiles(attachments);
        tenderRepository.delete(tender);
    }

    @Transactional
    public TenderDto addAttachments(Long tenderId, List<Long> fileIds) {
        Tender tender = tenderRepository.findById(tenderId)
                .orElseThrow(() -> new BusinessException(404, "招标不存在"));
        List<TenderAttachment> existing = tenderAttachmentRepository.findDetailsByTenderId(tenderId);
        List<Long> mergedFileIds = new ArrayList<>();
        for (TenderAttachment item : existing) {
            mergedFileIds.add(item.getFileStorage().getId());
        }
        for (Long fileId : uniqueFileIds(fileIds)) {
            if (!mergedFileIds.contains(fileId)) {
                mergedFileIds.add(fileId);
            }
        }
        syncAttachments(tender, mergedFileIds);
        return getTender(tenderId);
    }

    @Transactional
    public TenderDto removeAttachment(Long tenderId, Long attachmentId) {
        Tender tender = tenderRepository.findById(tenderId)
                .orElseThrow(() -> new BusinessException(404, "招标不存在"));
        TenderAttachment attachment = tenderAttachmentRepository.findDetailByIdAndTenderId(attachmentId, tenderId)
                .orElseThrow(() -> new BusinessException(404, "招标附件不存在"));
        tenderAttachmentRepository.delete(attachment);
        cleanupUnreferencedFiles(java.util.Collections.singletonList(attachment));
        tenderRepository.save(tender);
        return getTender(tenderId);
    }

    private void validateRequest(TenderUpsertRequest request, Long currentTenderId) {
        if (request.getSignupDeadline().isAfter(request.getDeadline())) {
            throw new BusinessException(400, "报名截止时间不能晚于项目截止时间");
        }
        if (request.getPublishAt().isAfter(request.getDeadline())) {
            throw new BusinessException(400, "发布时间不能晚于项目截止时间");
        }
        boolean projectCodeExists = currentTenderId == null
                ? tenderRepository.existsByProjectCode(request.getProjectCode())
                : tenderRepository.existsByProjectCodeAndIdNot(request.getProjectCode(), currentTenderId);
        if (projectCodeExists) {
            throw new BusinessException(400, "项目编号已存在");
        }
    }

    private void applyRequest(Tender tender, TenderUpsertRequest request, boolean creating) {
        String operator = currentOperatorUsername();
        tender.setTitle(request.getTitle());
        tender.setRegion(request.getRegion());
        tender.setPublishAt(request.getPublishAt());
        tender.setContent(request.getContent());
        tender.setContactPerson(request.getContactPerson());
        tender.setBudget(request.getBudget());
        tender.setContactPhone(request.getContactPhone());
        tender.setTenderUnit(request.getTenderUnit());
        tender.setDeadline(request.getDeadline());
        tender.setProjectCode(request.getProjectCode());
        tender.setSignupDeadline(request.getSignupDeadline());
        tender.setStatus(request.getStatus() == null ? TenderStatus.PUBLISHED : request.getStatus());
        if (creating) {
            tender.setCreatedBy(operator);
        }
        tender.setUpdatedBy(operator);
    }

    private void syncAttachments(Tender tender, List<Long> fileIds) {
        List<Long> normalizedFileIds = uniqueFileIds(fileIds);
        List<TenderAttachment> existing = tenderAttachmentRepository.findDetailsByTenderId(tender.getId());
        Map<Long, TenderAttachment> existingByFileId = existing.stream()
                .collect(Collectors.toMap(item -> item.getFileStorage().getId(), item -> item, (left, right) -> left, LinkedHashMap::new));
        List<TenderFileStorage> fileStorages = tenderFileStorageRepository.findAllById(normalizedFileIds);
        if (fileStorages.size() != normalizedFileIds.size()) {
            throw new BusinessException(400, "包含不存在的附件文件");
        }
        Map<Long, TenderFileStorage> fileStorageMap = fileStorages.stream()
                .collect(Collectors.toMap(TenderFileStorage::getId, item -> item));

        List<TenderAttachment> toDelete = existing.stream()
                .filter(item -> !normalizedFileIds.contains(item.getFileStorage().getId()))
                .collect(Collectors.toList());
        if (!toDelete.isEmpty()) {
            tenderAttachmentRepository.deleteAll(toDelete);
        }

        List<TenderAttachment> toSave = new ArrayList<>();
        for (int i = 0; i < normalizedFileIds.size(); i++) {
            Long fileId = normalizedFileIds.get(i);
            TenderAttachment attachment = existingByFileId.get(fileId);
            if (attachment == null) {
                attachment = new TenderAttachment();
                attachment.setTender(tender);
                attachment.setFileStorage(fileStorageMap.get(fileId));
            }
            attachment.setSortOrder(i + 1);
            toSave.add(attachment);
        }
        if (!toSave.isEmpty()) {
            tenderAttachmentRepository.saveAll(toSave);
        }
        cleanupUnreferencedFiles(toDelete);
    }

    private void cleanupUnreferencedFiles(List<TenderAttachment> attachments) {
        for (TenderAttachment attachment : attachments) {
            TenderFileStorage fileStorage = attachment.getFileStorage();
            if (tenderAttachmentRepository.countByFileStorage_Id(fileStorage.getId()) == 0) {
                localFileStorageService.deleteStoredFile(fileStorage);
                tenderFileStorageRepository.delete(fileStorage);
            }
        }
    }

    private List<Long> uniqueFileIds(List<Long> fileIds) {
        if (fileIds == null) {
            return java.util.Collections.emptyList();
        }
        Set<Long> set = fileIds.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        return new ArrayList<>(set);
    }

    private Pageable buildPageable(int pageNum, int pageSize) {
        int safePageNum = pageNum <= 0 ? 1 : pageNum;
        int safePageSize = pageSize <= 0 ? 10 : Math.min(pageSize, 50);
        return PageRequest.of(safePageNum - 1, safePageSize, Sort.by(Sort.Order.desc("publishAt"), Sort.Order.desc("id")));
    }

    private String normalize(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private PageResult<TenderListItemDto> toPageResult(Page<Tender> page, int pageNum, int pageSize) {
        PageResult<TenderListItemDto> result = new PageResult<>();
        result.setPageNum(pageNum <= 0 ? 1 : pageNum);
        result.setPageSize(pageSize <= 0 ? 10 : Math.min(pageSize, 50));
        result.setTotal(page.getTotalElements());
        result.setTotalPages(page.getTotalPages());
        result.setList(page.getContent().stream().map(this::toListItemDto).collect(Collectors.toList()));
        return result;
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

    private TenderDto toAdminDto(Tender tender, List<TenderAttachment> attachments) {
        TenderDto dto = new TenderDto();
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
        dto.setAttachments(attachments.stream().map(this::toAttachmentDto).collect(Collectors.toList()));
        dto.setCreatedBy(tender.getCreatedBy());
        dto.setUpdatedBy(tender.getUpdatedBy());
        dto.setCreatedAt(tender.getCreatedAt());
        dto.setUpdatedAt(tender.getUpdatedAt());
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

    private String currentOperatorUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser) {
            return ((LoginUser) authentication.getPrincipal()).getUsername();
        }
        return "system";
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
