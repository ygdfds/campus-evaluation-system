package com.campus.evaluation.school.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.evaluation.common.core.domain.PageResult;
import com.campus.evaluation.common.core.exception.BusinessException;
import com.campus.evaluation.common.security.SecurityUtils;
import com.campus.evaluation.school.domain.dto.ClassGroupDTO;
import com.campus.evaluation.school.domain.entity.ClassGroup;
import com.campus.evaluation.school.domain.entity.TeachingOrgUnit;
import com.campus.evaluation.school.domain.vo.ClassGroupVO;
import com.campus.evaluation.school.domain.vo.OptionVO;
import com.campus.evaluation.school.mapper.ClassGroupMapper;
import com.campus.evaluation.school.mapper.TeachingOrgMapper;
import com.campus.evaluation.school.service.ClassGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClassGroupServiceImpl implements ClassGroupService {

    private final ClassGroupMapper classGroupMapper;
    private final TeachingOrgMapper teachingOrgMapper;

    @Override
    public PageResult<ClassGroupVO> list(String keyword, Long teachingOrgId, String grade, String status, int pageNum, int pageSize) {
        Long tenantId = requireTenantId();
        LambdaQueryWrapper<ClassGroup> wrapper = new LambdaQueryWrapper<ClassGroup>()
                .eq(ClassGroup::getTenantId, tenantId)
                .eq(teachingOrgId != null, ClassGroup::getTeachingOrgId, teachingOrgId)
                .eq(grade != null, ClassGroup::getGradeName, grade)
                .eq(status != null, ClassGroup::getStatus, status)
                .like(keyword != null && !keyword.isEmpty(), ClassGroup::getClassName, keyword)
                .orderByAsc(ClassGroup::getId);
        Page<ClassGroup> page = classGroupMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        List<ClassGroupVO> records = page.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return new PageResult<>(page.getTotal(), records, pageNum, pageSize);
    }

    @Override
    public ClassGroupVO create(ClassGroupDTO dto) {
        Long tenantId = requireTenantId();
        verifyTeachingOrg(tenantId, dto.getTeachingOrgId());
        ClassGroup entity = new ClassGroup();
        entity.setTenantId(tenantId);
        entity.setSchoolId(SecurityUtils.getSchoolId());
        entity.setTeachingOrgId(dto.getTeachingOrgId());
        entity.setGradeName(dto.getGradeName());
        entity.setClassName(dto.getClassName());
        entity.setStatus(dto.getStatus());
        classGroupMapper.insert(entity);
        return toVO(entity);
    }

    @Override
    public ClassGroupVO update(Long id, ClassGroupDTO dto) {
        Long tenantId = requireTenantId();
        ClassGroup entity = getByIdAndTenant(id, tenantId);
        verifyTeachingOrg(tenantId, dto.getTeachingOrgId());
        entity.setTeachingOrgId(dto.getTeachingOrgId());
        entity.setGradeName(dto.getGradeName());
        entity.setClassName(dto.getClassName());
        if (dto.getStatus() != null) entity.setStatus(dto.getStatus());
        classGroupMapper.updateById(entity);
        return toVO(entity);
    }

    @Override
    public void delete(Long id) {
        Long tenantId = requireTenantId();
        getByIdAndTenant(id, tenantId);
        classGroupMapper.deleteById(id);
    }

    @Override
    public List<OptionVO> options(Long teachingOrgId) {
        Long tenantId = requireTenantId();
        LambdaQueryWrapper<ClassGroup> wrapper = new LambdaQueryWrapper<ClassGroup>()
                .eq(ClassGroup::getTenantId, tenantId)
                .eq(ClassGroup::getStatus, "active")
                .eq(teachingOrgId != null, ClassGroup::getTeachingOrgId, teachingOrgId)
                .orderByAsc(ClassGroup::getClassName);
        return classGroupMapper.selectList(wrapper).stream()
                .map(c -> OptionVO.builder().id(c.getId()).label(c.getClassName()).value(c.getId()).extra(c.getGradeName()).build())
                .collect(Collectors.toList());
    }

    private void verifyTeachingOrg(Long tenantId, Long teachingOrgId) {
        TeachingOrgUnit org = teachingOrgMapper.selectOne(
                new LambdaQueryWrapper<TeachingOrgUnit>()
                        .eq(TeachingOrgUnit::getId, teachingOrgId)
                        .eq(TeachingOrgUnit::getTenantId, tenantId)
        );
        if (org == null) throw new BusinessException(404, "教学组织不存在");
    }

    private ClassGroup getByIdAndTenant(Long id, Long tenantId) {
        ClassGroup entity = classGroupMapper.selectOne(
                new LambdaQueryWrapper<ClassGroup>()
                        .eq(ClassGroup::getId, id)
                        .eq(ClassGroup::getTenantId, tenantId)
        );
        if (entity == null) throw new BusinessException(404, "班级不存在");
        return entity;
    }

    private Long requireTenantId() {
        Long tenantId = SecurityUtils.getTenantId();
        if (tenantId == null) throw new BusinessException(403, "无法获取租户信息");
        return tenantId;
    }

    private ClassGroupVO toVO(ClassGroup e) {
        return ClassGroupVO.builder()
                .id(e.getId()).tenantId(e.getTenantId()).schoolId(e.getSchoolId())
                .teachingOrgId(e.getTeachingOrgId()).gradeName(e.getGradeName())
                .className(e.getClassName()).status(e.getStatus())
                .createdAt(e.getCreatedAt()).updatedAt(e.getUpdatedAt())
                .build();
    }
}
