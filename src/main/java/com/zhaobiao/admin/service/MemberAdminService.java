package com.zhaobiao.admin.service;

import com.zhaobiao.admin.common.BusinessException;
import com.zhaobiao.admin.dto.member.MemberCreateRequest;
import com.zhaobiao.admin.dto.member.MemberDownloadAccessUpdateRequest;
import com.zhaobiao.admin.dto.member.MemberPasswordResetRequest;
import com.zhaobiao.admin.dto.member.MemberStatusUpdateRequest;
import com.zhaobiao.admin.dto.member.MemberUpdateRequest;
import com.zhaobiao.admin.dto.member.MemberUserDto;
import com.zhaobiao.admin.entity.BusinessType;
import com.zhaobiao.admin.entity.MemberUser;
import com.zhaobiao.admin.entity.MemberUserStatus;
import com.zhaobiao.admin.mapper.ViewMapper;
import com.zhaobiao.admin.repository.BusinessTypeRepository;
import com.zhaobiao.admin.repository.MemberUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class MemberAdminService {

    private final MemberUserRepository memberUserRepository;
    private final BusinessTypeRepository businessTypeRepository;
    private final PasswordEncoder passwordEncoder;
    private final PortalAuthService portalAuthService;
    private final ViewMapper viewMapper;

    public MemberAdminService(MemberUserRepository memberUserRepository,
                              BusinessTypeRepository businessTypeRepository,
                              PasswordEncoder passwordEncoder,
                              PortalAuthService portalAuthService,
                              ViewMapper viewMapper) {
        this.memberUserRepository = memberUserRepository;
        this.businessTypeRepository = businessTypeRepository;
        this.passwordEncoder = passwordEncoder;
        this.portalAuthService = portalAuthService;
        this.viewMapper = viewMapper;
    }

    @Transactional(readOnly = true)
    public List<MemberUserDto> listMembers() {
        return memberUserRepository.findAllWithDetails().stream()
                .sorted(Comparator.comparing(MemberUser::getCreatedAt).reversed())
                .map(viewMapper::toMemberUserDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MemberUserDto getMemberDetail(Long memberId) {
        return viewMapper.toMemberUserDto(getMember(memberId));
    }

    @Transactional
    public MemberUserDto createMember(MemberCreateRequest request) {
        validatePassword(request.getPassword(), request.getConfirmPassword());
        portalAuthService.ensureMemberUnique(
                request.getUsername(),
                request.getPhone(),
                request.getEmail(),
                request.getUnifiedSocialCreditCode(),
                null
        );

        MemberUser user = new MemberUser();
        user.setUsername(request.getUsername().trim());
        user.setPhone(request.getPhone().trim());
        user.setEmail(request.getEmail().trim());
        user.setCompanyName(request.getCompanyName().trim());
        user.setContactPerson(request.getContactPerson().trim());
        user.setUnifiedSocialCreditCode(request.getUnifiedSocialCreditCode().trim());
        user.setRealName(request.getRealName());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setStatus(request.getStatus() == null ? MemberUserStatus.ENABLED : request.getStatus());
        user.setCanDownloadFile(Boolean.TRUE.equals(request.getCanDownloadFile()));
        user.setBusinessTypes(loadEnabledBusinessTypes(request.getBusinessTypeIds()));
        return viewMapper.toMemberUserDto(memberUserRepository.save(user));
    }

    @Transactional
    public MemberUserDto updateMember(Long memberId, MemberUpdateRequest request) {
        MemberUser user = getMember(memberId);
        portalAuthService.ensureMemberUnique(
                null,
                request.getPhone(),
                request.getEmail(),
                request.getUnifiedSocialCreditCode(),
                memberId
        );
        user.setPhone(request.getPhone().trim());
        user.setEmail(request.getEmail().trim());
        user.setCompanyName(request.getCompanyName().trim());
        user.setContactPerson(request.getContactPerson().trim());
        user.setUnifiedSocialCreditCode(request.getUnifiedSocialCreditCode().trim());
        user.setRealName(request.getRealName());
        user.setBusinessTypes(loadEnabledBusinessTypes(request.getBusinessTypeIds()));
        return viewMapper.toMemberUserDto(memberUserRepository.save(user));
    }

    @Transactional
    public MemberUserDto updateDownloadAccess(Long memberId, MemberDownloadAccessUpdateRequest request) {
        MemberUser user = getMember(memberId);
        user.setCanDownloadFile(Boolean.TRUE.equals(request.getCanDownloadFile()));
        return viewMapper.toMemberUserDto(memberUserRepository.save(user));
    }

    @Transactional
    public MemberUserDto updateStatus(Long memberId, MemberStatusUpdateRequest request) {
        MemberUser user = getMember(memberId);
        user.setStatus(request.getStatus());
        return viewMapper.toMemberUserDto(memberUserRepository.save(user));
    }

    @Transactional
    public void resetPassword(Long memberId, MemberPasswordResetRequest request) {
        MemberUser user = getMember(memberId);
        validatePassword(request.getPassword(), request.getConfirmPassword());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        memberUserRepository.save(user);
    }

    private MemberUser getMember(Long memberId) {
        return memberUserRepository.findDetailById(memberId)
                .orElseThrow(() -> new BusinessException(404, "会员不存在"));
    }

    private Set<BusinessType> loadEnabledBusinessTypes(List<Long> businessTypeIds) {
        if (businessTypeIds == null || businessTypeIds.isEmpty()) {
            throw new BusinessException(400, "会员类型至少选择一个");
        }
        List<Long> normalizedIds = businessTypeIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (normalizedIds.isEmpty()) {
            throw new BusinessException(400, "会员类型至少选择一个");
        }
        List<BusinessType> businessTypes = businessTypeRepository.findAllById(normalizedIds);
        if (businessTypes.size() != normalizedIds.size()) {
            throw new BusinessException(400, "包含不存在的业务类型");
        }
        if (businessTypes.stream().anyMatch(item -> !item.isEnabled())) {
            throw new BusinessException(400, "不能绑定已禁用的业务类型");
        }
        return businessTypes.stream()
                .sorted(Comparator.comparing(BusinessType::getSortOrder).thenComparing(BusinessType::getId))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private void validatePassword(String password, String confirmPassword) {
        if (password == null || !password.equals(confirmPassword)) {
            throw new BusinessException(400, "两次输入的密码不一致");
        }
    }
}
