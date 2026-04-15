package com.zhaobiao.admin.service;

import com.zhaobiao.admin.common.BusinessException;
import com.zhaobiao.admin.dto.member.MemberDownloadAccessUpdateRequest;
import com.zhaobiao.admin.dto.member.MemberStatusUpdateRequest;
import com.zhaobiao.admin.dto.member.MemberUserDto;
import com.zhaobiao.admin.entity.MemberUser;
import com.zhaobiao.admin.repository.MemberUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MemberAdminService {

    private final MemberUserRepository memberUserRepository;

    public MemberAdminService(MemberUserRepository memberUserRepository) {
        this.memberUserRepository = memberUserRepository;
    }

    @Transactional(readOnly = true)
    public List<MemberUserDto> listMembers() {
        return memberUserRepository.findAll().stream()
                .sorted(Comparator.comparing(MemberUser::getCreatedAt).reversed())
                .map(this::toMemberDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public MemberUserDto updateDownloadAccess(Long memberId, MemberDownloadAccessUpdateRequest request) {
        MemberUser user = getMember(memberId);
        user.setCanDownloadFile(Boolean.TRUE.equals(request.getCanDownloadFile()));
        return toMemberDto(memberUserRepository.save(user));
    }

    @Transactional
    public MemberUserDto updateStatus(Long memberId, MemberStatusUpdateRequest request) {
        MemberUser user = getMember(memberId);
        user.setStatus(request.getStatus());
        return toMemberDto(memberUserRepository.save(user));
    }

    private MemberUser getMember(Long memberId) {
        return memberUserRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(404, "会员不存在"));
    }

    private MemberUserDto toMemberDto(MemberUser user) {
        MemberUserDto dto = new MemberUserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setPhone(user.getPhone());
        dto.setEmail(user.getEmail());
        dto.setRealName(user.getRealName());
        dto.setCompanyName(user.getCompanyName());
        dto.setContactPerson(user.getContactPerson());
        dto.setUnifiedSocialCreditCode(user.getUnifiedSocialCreditCode());
        dto.setCanDownloadFile(user.isCanDownloadFile());
        dto.setStatus(user.getStatus());
        dto.setLastLoginAt(user.getLastLoginAt());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }
}
