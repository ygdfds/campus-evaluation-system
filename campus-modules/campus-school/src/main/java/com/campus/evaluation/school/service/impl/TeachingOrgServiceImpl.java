package com.campus.evaluation.school.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.evaluation.common.core.domain.PageResult;
import com.campus.evaluation.common.core.exception.BusinessException;
import com.campus.evaluation.common.security.SecurityUtils;
import com.campus.evaluation.school.domain.dto.OrgUnitDTO;
import com.campus.evaluation.school.domain.entity.TeachingOrgUnit;
import com.campus.evaluation.school.domain.vo.OrgTreeVO;
import com.campus.evaluation.school.domain.vo.TeachingOrgVO;
import com.campus.evaluation.school.mapper.TeachingOrgMapper;
import com.campus.evaluation.school.service.TeachingOrgService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeachingOrgServiceImpl implements TeachingOrgService {

    private final TeachingOrgMapper teachingOrgMapper;

    @Override
    public PageResult<TeachingOrgVO> list(String keyword, String status, Long parentId, int pageNum, int pageSize) {
        Long tenantId = requireTenantId();
        LambdaQueryWrapper<TeachingOrgUnit> wrapper = new LambdaQueryWrapper<TeachingOrgUnit>()
                .eq(TeachingOrgUnit::getTenantId, tenantId)
                .eq(status != null, TeachingOrgUnit::getStatus, status)
                .eq(parentId != null, TeachingOrgUnit::getParentId, parentId)
                .like(keyword != null && !keyword.isEmpty(), TeachingOrgUnit::getName, keyword)
                .orderByAsc(TeachingOrgUnit::getId);
        Page<TeachingOrgUnit> page = teachingOrgMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        List<TeachingOrgVO> records = page.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return new PageResult<>(page.getTotal(), records, pageNum, pageSize);
    }

    @Override
    public List<OrgTreeVO> tree() {
        Long tenantId = requireTenantId();
        List<TeachingOrgUnit> all = teachingOrgMapper.selectList(
                new LambdaQueryWrapper<TeachingOrgUnit>()
                        .eq(TeachingOrgUnit::getTenantId, tenantId)
                        .orderByAsc(TeachingOrgUnit::getId)
        );
        return buildTree(all.stream().map(this::toTreeNode).collect(Collectors.toList()));
    }

    @Override
    public TeachingOrgVO create(OrgUnitDTO dto) {
        Long tenantId = requireTenantId();
        checkCodeUnique(tenantId, dto.getCode(), null);
        validateParentId(dto.getParentId(), null);

        TeachingOrgUnit entity = new TeachingOrgUnit();
        entity.setTenantId(tenantId);
        entity.setSchoolId(SecurityUtils.getSchoolId());
        entity.setParentId(dto.getParentId());
        entity.setName(dto.getName());
        entity.setCode(dto.getCode());
        entity.setType(dto.getType());
        entity.setStatus(dto.getStatus());
        teachingOrgMapper.insert(entity);
        return toVO(entity);
    }

    @Override
    public TeachingOrgVO update(Long id, OrgUnitDTO dto) {
        Long tenantId = requireTenantId();
        TeachingOrgUnit entity = getByIdAndTenant(id, tenantId);
        checkCodeUnique(tenantId, dto.getCode(), id);
        validateParentId(dto.getParentId(), id);

        entity.setName(dto.getName());
        entity.setCode(dto.getCode());
        entity.setType(dto.getType());
        entity.setParentId(dto.getParentId());
        if (dto.getStatus() != null) entity.setStatus(dto.getStatus());
        teachingOrgMapper.updateById(entity);
        return toVO(entity);
    }

    @Override
    public void delete(Long id) {
        Long tenantId = requireTenantId();
        getByIdAndTenant(id, tenantId);
        long childCount = teachingOrgMapper.selectCount(
                new LambdaQueryWrapper<TeachingOrgUnit>()
                        .eq(TeachingOrgUnit::getTenantId, tenantId)
                        .eq(TeachingOrgUnit::getParentId, id)
        );
        if (childCount > 0) {
            throw new BusinessException(409, "存在子组织，无法删除，请先处理子组织");
        }
        teachingOrgMapper.deleteById(id);
    }

    private TeachingOrgUnit getByIdAndTenant(Long id, Long tenantId) {
        TeachingOrgUnit entity = teachingOrgMapper.selectOne(
                new LambdaQueryWrapper<TeachingOrgUnit>()
                        .eq(TeachingOrgUnit::getId, id)
                        .eq(TeachingOrgUnit::getTenantId, tenantId)
        );
        if (entity == null) {
            throw new BusinessException(404, "教学组织不存在");
        }
        return entity;
    }

    private void checkCodeUnique(Long tenantId, String code, Long excludeId) {
        long count = teachingOrgMapper.selectCount(
                new LambdaQueryWrapper<TeachingOrgUnit>()
                        .eq(TeachingOrgUnit::getTenantId, tenantId)
                        .eq(TeachingOrgUnit::getCode, code)
                        .ne(excludeId != null, TeachingOrgUnit::getId, excludeId)
        );
        if (count > 0) {
            throw new BusinessException(409, "组织编码已存在");
        }
    }

    private void validateParentId(Long parentId, Long selfId) {
        if (parentId != null && selfId != null && parentId.equals(selfId)) {
            throw new BusinessException(400, "父级不能指向自己");
        }
    }

    private Long requireTenantId() {
        Long tenantId = SecurityUtils.getTenantId();
        if (tenantId == null) throw new BusinessException(403, "无法获取租户信息");
        return tenantId;
    }

    private TeachingOrgVO toVO(TeachingOrgUnit e) {
        return TeachingOrgVO.builder()
                .id(e.getId()).tenantId(e.getTenantId()).schoolId(e.getSchoolId())
                .parentId(e.getParentId()).name(e.getName()).code(e.getCode())
                .type(e.getType()).status(e.getStatus())
                .createdAt(e.getCreatedAt()).updatedAt(e.getUpdatedAt())
                .build();
    }

    private OrgTreeVO toTreeNode(TeachingOrgUnit e) {
        OrgTreeVO node = new OrgTreeVO();
        node.setId(e.getId());
        node.setParentId(e.getParentId());
        node.setName(e.getName());
        node.setCode(e.getCode());
        node.setType(e.getType());
        node.setStatus(e.getStatus());
        return node;
    }

    private List<OrgTreeVO> buildTree(List<OrgTreeVO> nodes) {
        Map<Long, List<OrgTreeVO>> childMap = nodes.stream()
                .filter(n -> n.getParentId() != null)
                .collect(Collectors.groupingBy(OrgTreeVO::getParentId));
        nodes.forEach(n -> n.setChildren(childMap.getOrDefault(n.getId(), new ArrayList<>())));
        return nodes.stream().filter(n -> n.getParentId() == null).collect(Collectors.toList());
    }
}
