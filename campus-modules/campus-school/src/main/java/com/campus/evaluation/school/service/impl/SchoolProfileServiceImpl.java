package com.campus.evaluation.school.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.evaluation.common.core.exception.BusinessException;
import com.campus.evaluation.common.security.SecurityUtils;
import com.campus.evaluation.school.domain.dto.SchoolProfileUpdateDTO;
import com.campus.evaluation.school.domain.entity.SchoolProfile;
import com.campus.evaluation.school.domain.vo.SchoolProfileVO;
import com.campus.evaluation.school.mapper.SchoolProfileMapper;
import com.campus.evaluation.school.service.SchoolProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SchoolProfileServiceImpl implements SchoolProfileService {

    private final SchoolProfileMapper schoolProfileMapper;

    @Override
    public SchoolProfileVO getCurrentProfile() {
        Long tenantId = SecurityUtils.getTenantId();
        if (tenantId == null) {
            throw new BusinessException(403, "无法获取租户信息");
        }
        SchoolProfile profile = schoolProfileMapper.selectOne(
                new LambdaQueryWrapper<SchoolProfile>()
                        .eq(SchoolProfile::getTenantId, tenantId)
        );
        if (profile == null) {
            throw new BusinessException(404, "学校资料不存在");
        }
        return toVO(profile);
    }

    @Override
    public SchoolProfileVO updateCurrentProfile(SchoolProfileUpdateDTO dto) {
        Long tenantId = SecurityUtils.getTenantId();
        if (tenantId == null) {
            throw new BusinessException(403, "无法获取租户信息");
        }
        SchoolProfile profile = schoolProfileMapper.selectOne(
                new LambdaQueryWrapper<SchoolProfile>()
                        .eq(SchoolProfile::getTenantId, tenantId)
        );
        if (profile == null) {
            throw new BusinessException(404, "学校资料不存在");
        }

        if (dto.getName() != null) profile.setName(dto.getName());
        if (dto.getAddress() != null) profile.setAddress(dto.getAddress());
        if (dto.getWebsite() != null) profile.setWebsite(dto.getWebsite());
        if (dto.getLogoFileId() != null) profile.setLogoFileId(dto.getLogoFileId());
        if (dto.getCoverFileId() != null) profile.setCoverFileId(dto.getCoverFileId());
        if (dto.getIntro() != null) profile.setIntro(dto.getIntro());

        schoolProfileMapper.updateById(profile);
        return toVO(profile);
    }

    private SchoolProfileVO toVO(SchoolProfile p) {
        return SchoolProfileVO.builder()
                .id(p.getId())
                .tenantId(p.getTenantId())
                .schoolName(p.getName())
                .address(p.getAddress())
                .website(p.getWebsite())
                .logoFileId(p.getLogoFileId())
                .coverFileId(p.getCoverFileId())
                .description(p.getIntro())
                .status(p.getStatus())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}
