package com.zhaobiao.admin.service;

import com.zhaobiao.admin.common.BusinessException;
import com.zhaobiao.admin.dto.business.BusinessTypeCreateRequest;
import com.zhaobiao.admin.dto.business.BusinessTypeDto;
import com.zhaobiao.admin.dto.business.BusinessTypeOptionDto;
import com.zhaobiao.admin.dto.business.BusinessTypeStatusUpdateRequest;
import com.zhaobiao.admin.dto.business.BusinessTypeUpdateRequest;
import com.zhaobiao.admin.entity.BusinessType;
import com.zhaobiao.admin.mapper.ViewMapper;
import com.zhaobiao.admin.repository.BusinessTypeRepository;
import com.zhaobiao.admin.repository.MemberUserRepository;
import com.zhaobiao.admin.repository.TenderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class BusinessTypeAdminService {

    private final BusinessTypeRepository businessTypeRepository;
    private final MemberUserRepository memberUserRepository;
    private final TenderRepository tenderRepository;
    private final ViewMapper viewMapper;

    public BusinessTypeAdminService(BusinessTypeRepository businessTypeRepository,
                                    MemberUserRepository memberUserRepository,
                                    TenderRepository tenderRepository,
                                    ViewMapper viewMapper) {
        this.businessTypeRepository = businessTypeRepository;
        this.memberUserRepository = memberUserRepository;
        this.tenderRepository = tenderRepository;
        this.viewMapper = viewMapper;
    }

    @Transactional(readOnly = true)
    public List<BusinessTypeDto> listAll() {
        return businessTypeRepository.findAllByOrderBySortOrderAscIdAsc().stream()
                .map(viewMapper::toBusinessTypeDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<BusinessTypeOptionDto> listEnabledOptions() {
        return businessTypeRepository.findByEnabledTrueOrderBySortOrderAscIdAsc().stream()
                .map(viewMapper::toBusinessTypeOptionDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public BusinessTypeDto create(BusinessTypeCreateRequest request) {
        ensureUnique(normalizeCode(request.getCode()), request.getName(), null);
        BusinessType businessType = new BusinessType();
        applyRequest(businessType, normalizeCode(request.getCode()), request.getName(), request.getEnabled(),
                request.getSortOrder(), request.getDescription());
        return viewMapper.toBusinessTypeDto(businessTypeRepository.save(businessType));
    }

    @Transactional
    public BusinessTypeDto update(Long businessTypeId, BusinessTypeUpdateRequest request) {
        BusinessType businessType = getBusinessType(businessTypeId);
        String code = normalizeCode(request.getCode());
        ensureUnique(code, request.getName(), businessTypeId);
        applyRequest(businessType, code, request.getName(), request.getEnabled(), request.getSortOrder(), request.getDescription());
        return viewMapper.toBusinessTypeDto(businessTypeRepository.save(businessType));
    }

    @Transactional
    public BusinessTypeDto updateStatus(Long businessTypeId, BusinessTypeStatusUpdateRequest request) {
        BusinessType businessType = getBusinessType(businessTypeId);
        businessType.setEnabled(Boolean.TRUE.equals(request.getEnabled()));
        return viewMapper.toBusinessTypeDto(businessTypeRepository.save(businessType));
    }

    @Transactional
    public void delete(Long businessTypeId) {
        BusinessType businessType = getBusinessType(businessTypeId);
        if (memberUserRepository.existsByBusinessTypes_Id(businessTypeId) || tenderRepository.existsByBusinessType_Id(businessTypeId)) {
            throw new BusinessException(400, "该类型已被会员或招标引用，请先停用，不能删除");
        }
        businessTypeRepository.delete(businessType);
    }

    private BusinessType getBusinessType(Long businessTypeId) {
        return businessTypeRepository.findById(businessTypeId)
                .orElseThrow(() -> new BusinessException(404, "业务类型不存在"));
    }

    private void ensureUnique(String code, String name, Long businessTypeId) {
        boolean codeExists = businessTypeId == null
                ? businessTypeRepository.existsByCode(code)
                : businessTypeRepository.existsByCodeAndIdNot(code, businessTypeId);
        if (codeExists) {
            throw new BusinessException(400, "类型编码已存在");
        }
        boolean nameExists = businessTypeId == null
                ? businessTypeRepository.existsByName(name)
                : businessTypeRepository.existsByNameAndIdNot(name, businessTypeId);
        if (nameExists) {
            throw new BusinessException(400, "类型名称已存在");
        }
    }

    private void applyRequest(BusinessType businessType,
                              String code,
                              String name,
                              Boolean enabled,
                              Integer sortOrder,
                              String description) {
        businessType.setCode(code);
        businessType.setName(name.trim());
        businessType.setEnabled(Boolean.TRUE.equals(enabled));
        businessType.setSortOrder(sortOrder == null ? nextSortOrder() : sortOrder);
        businessType.setDescription(StringUtils.hasText(description) ? description.trim() : null);
    }

    private int nextSortOrder() {
        return businessTypeRepository.findTopByOrderBySortOrderDescIdDesc()
                .map(item -> item.getSortOrder() + 10)
                .orElse(10);
    }

    private String normalizeCode(String code) {
        return code == null ? null : code.trim().toUpperCase(Locale.ROOT);
    }
}
