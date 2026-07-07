package com.campus.evaluation.school.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.evaluation.common.core.domain.PageResult;
import com.campus.evaluation.common.core.exception.BusinessException;
import com.campus.evaluation.common.security.SecurityUtils;
import com.campus.evaluation.school.domain.dto.OrgUnitDTO;
import com.campus.evaluation.school.domain.entity.ServiceOrgUnit;
import com.campus.evaluation.school.domain.vo.OrgTreeVO;
import com.campus.evaluation.school.domain.vo.ServiceOrgVO;
import com.campus.evaluation.school.mapper.ServiceOrgMapper;
import com.campus.evaluation.school.service.ServiceOrgService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServiceOrgServiceImpl implements ServiceOrgService {

    private final ServiceOrgMapper serviceOrgMapper;

    @Override
    public PageResult<ServiceOrgVO> list(String keyword, String status, Long parentId, int pageNum, int pageSize) {
        Long tenantId = requireTenantId();
        LambdaQueryWrapper<ServiceOrgUnit> wrapper = new LambdaQueryWrapper<ServiceOrgUnit>()
                .eq(ServiceOrgUnit::getTenantId, tenantId)
                .eq(status != null, ServiceOrgUnit::getStatus, status)
                .eq(parentId != null, ServiceOrgUnit::getParentId, parentId)
                .like(keyword != null && !keyword.isEmpty(), ServiceOrgUnit::getName, keyword)
                .orderByAsc(ServiceOrgUnit::getId);
        Page<ServiceOrgUnit> page = serviceOrgMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        List<ServiceOrgVO> records = page.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return new PageResult<>(page.getTotal(), records, pageNum, pageSize);
    }

    @Override
    public List<OrgTreeVO> tree() {
        Long tenantId = requireTenantId();
        List<ServiceOrgUnit> all = serviceOrgMapper.selectList(
                new LambdaQueryWrapper<ServiceOrgUnit>()
                        .eq(ServiceOrgUnit::getTenantId, tenantId)
                        .orderByAsc(ServiceOrgUnit::getId)
        );
        return buildTree(all.stream().map(this::toTreeNode).collect(Collectors.toList()));
    }

    @Override
    public ServiceOrgVO create(OrgUnitDTO dto) {
        Long tenantId = requireTenantId();
        checkCodeUnique(tenantId, dto.getCode(), null);
        ServiceOrgUnit entity = new ServiceOrgUnit();
        entity.setTenantId(tenantId);
        entity.setSchoolId(SecurityUtils.getSchoolId());
        entity.setParentId(dto.getParentId());
        entity.setName(dto.getName());
        entity.setCode(dto.getCode());
        entity.setType(dto.getType());
        entity.setStatus(dto.getStatus());
        serviceOrgMapper.insert(entity);
        return toVO(entity);
    }

    @Override
    public ServiceOrgVO update(Long id, OrgUnitDTO dto) {
        Long tenantId = requireTenantId();
        ServiceOrgUnit entity = getByIdAndTenant(id, tenantId);
        checkCodeUnique(tenantId, dto.getCode(), id);
        entity.setName(dto.getName());
        entity.setCode(dto.getCode());
        entity.setType(dto.getType());
        entity.setParentId(dto.getParentId());
        if (dto.getStatus() != null) entity.setStatus(dto.getStatus());
        serviceOrgMapper.updateById(entity);
        return toVO(entity);
    }

    @Override
    public void delete(Long id) {
        Long tenantId = requireTenantId();
        getByIdAndTenant(id, tenantId);
        long childCount = serviceOrgMapper.selectCount(
                new LambdaQueryWrapper<ServiceOrgUnit>()
                        .eq(ServiceOrgUnit::getTenantId, tenantId)
                        .eq(ServiceOrgUnit::getParentId, id)
        );
        if (childCount > 0) {
            throw new BusinessException(409, "存在子组织，无法删除，请先处理子组织");
        }
        serviceOrgMapper.deleteById(id);
    }

    private ServiceOrgUnit getByIdAndTenant(Long id, Long tenantId) {
        ServiceOrgUnit entity = serviceOrgMapper.selectOne(
                new LambdaQueryWrapper<ServiceOrgUnit>()
                        .eq(ServiceOrgUnit::getId, id)
                        .eq(ServiceOrgUnit::getTenantId, tenantId)
        );
        if (entity == null) throw new BusinessException(404, "服务组织不存在");
        return entity;
    }

    private void checkCodeUnique(Long tenantId, String code, Long excludeId) {
        long count = serviceOrgMapper.selectCount(
                new LambdaQueryWrapper<ServiceOrgUnit>()
                        .eq(ServiceOrgUnit::getTenantId, tenantId)
                        .eq(ServiceOrgUnit::getCode, code)
                        .ne(excludeId != null, ServiceOrgUnit::getId, excludeId)
        );
        if (count > 0) throw new BusinessException(409, "组织编码已存在");
    }

    private Long requireTenantId() {
        Long tenantId = SecurityUtils.getTenantId();
        if (tenantId == null) throw new BusinessException(403, "无法获取租户信息");
        return tenantId;
    }

    private ServiceOrgVO toVO(ServiceOrgUnit e) {
        return ServiceOrgVO.builder()
                .id(e.getId()).tenantId(e.getTenantId()).schoolId(e.getSchoolId())
                .parentId(e.getParentId()).name(e.getName()).code(e.getCode())
                .type(e.getType()).status(e.getStatus())
                .createdAt(e.getCreatedAt()).updatedAt(e.getUpdatedAt())
                .build();
    }

    private OrgTreeVO toTreeNode(ServiceOrgUnit e) {
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
