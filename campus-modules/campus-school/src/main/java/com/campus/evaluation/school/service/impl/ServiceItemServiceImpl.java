package com.campus.evaluation.school.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.evaluation.common.core.domain.PageResult;
import com.campus.evaluation.common.core.exception.BusinessException;
import com.campus.evaluation.common.security.SecurityUtils;
import com.campus.evaluation.school.domain.dto.ServiceItemDTO;
import com.campus.evaluation.school.domain.entity.ServiceItem;
import com.campus.evaluation.school.domain.entity.ServiceOrgUnit;
import com.campus.evaluation.school.domain.vo.OptionVO;
import com.campus.evaluation.school.domain.vo.ServiceItemVO;
import com.campus.evaluation.school.mapper.ServiceItemMapper;
import com.campus.evaluation.school.mapper.ServiceOrgMapper;
import com.campus.evaluation.school.service.ServiceItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServiceItemServiceImpl implements ServiceItemService {

    private final ServiceItemMapper serviceItemMapper;
    private final ServiceOrgMapper serviceOrgMapper;

    @Override
    public PageResult<ServiceItemVO> list(String keyword, Long serviceOrgId, String status, int pageNum, int pageSize) {
        Long tenantId = requireTenantId();
        LambdaQueryWrapper<ServiceItem> wrapper = new LambdaQueryWrapper<ServiceItem>()
                .eq(ServiceItem::getTenantId, tenantId)
                .eq(serviceOrgId != null, ServiceItem::getServiceOrgId, serviceOrgId)
                .eq(status != null, ServiceItem::getStatus, status)
                .like(keyword != null && !keyword.isEmpty(), ServiceItem::getName, keyword)
                .orderByAsc(ServiceItem::getId);
        Page<ServiceItem> page = serviceItemMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        List<ServiceItemVO> records = page.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return new PageResult<>(page.getTotal(), records, pageNum, pageSize);
    }

    @Override
    public ServiceItemVO getDetail(Long id) {
        Long tenantId = requireTenantId();
        return toVO(getByIdAndTenant(id, tenantId));
    }

    @Override
    public ServiceItemVO create(ServiceItemDTO dto) {
        Long tenantId = requireTenantId();
        verifyServiceOrg(tenantId, dto.getServiceOrgId());
        ServiceItem entity = new ServiceItem();
        entity.setTenantId(tenantId);
        entity.setSchoolId(SecurityUtils.getSchoolId());
        entity.setServiceOrgId(dto.getServiceOrgId());
        entity.setName(dto.getName());
        entity.setCoverFileId(dto.getCoverFileId());
        entity.setType(dto.getType());
        entity.setStatus(dto.getStatus());
        serviceItemMapper.insert(entity);
        return toVO(entity);
    }

    @Override
    public ServiceItemVO update(Long id, ServiceItemDTO dto) {
        Long tenantId = requireTenantId();
        ServiceItem entity = getByIdAndTenant(id, tenantId);
        verifyServiceOrg(tenantId, dto.getServiceOrgId());
        entity.setServiceOrgId(dto.getServiceOrgId());
        entity.setName(dto.getName());
        entity.setCoverFileId(dto.getCoverFileId());
        entity.setType(dto.getType());
        if (dto.getStatus() != null) entity.setStatus(dto.getStatus());
        serviceItemMapper.updateById(entity);
        return toVO(entity);
    }

    @Override
    public void delete(Long id) {
        Long tenantId = requireTenantId();
        getByIdAndTenant(id, tenantId);
        serviceItemMapper.deleteById(id);
    }

    @Override
    public List<OptionVO> options(Long serviceOrgId) {
        Long tenantId = requireTenantId();
        LambdaQueryWrapper<ServiceItem> wrapper = new LambdaQueryWrapper<ServiceItem>()
                .eq(ServiceItem::getTenantId, tenantId)
                .eq(ServiceItem::getStatus, "active")
                .eq(serviceOrgId != null, ServiceItem::getServiceOrgId, serviceOrgId)
                .orderByAsc(ServiceItem::getName);
        return serviceItemMapper.selectList(wrapper).stream()
                .map(i -> OptionVO.builder().id(i.getId()).label(i.getName()).value(i.getId()).extra(i.getType()).build())
                .collect(Collectors.toList());
    }

    private ServiceItem getByIdAndTenant(Long id, Long tenantId) {
        ServiceItem entity = serviceItemMapper.selectOne(
                new LambdaQueryWrapper<ServiceItem>()
                        .eq(ServiceItem::getId, id)
                        .eq(ServiceItem::getTenantId, tenantId)
        );
        if (entity == null) throw new BusinessException(404, "服务项目不存在");
        return entity;
    }

    private void verifyServiceOrg(Long tenantId, Long serviceOrgId) {
        ServiceOrgUnit org = serviceOrgMapper.selectOne(
                new LambdaQueryWrapper<ServiceOrgUnit>()
                        .eq(ServiceOrgUnit::getId, serviceOrgId)
                        .eq(ServiceOrgUnit::getTenantId, tenantId)
        );
        if (org == null) throw new BusinessException(404, "服务组织不存在");
    }

    private Long requireTenantId() {
        Long tenantId = SecurityUtils.getTenantId();
        if (tenantId == null) throw new BusinessException(403, "无法获取租户信息");
        return tenantId;
    }

    private ServiceItemVO toVO(ServiceItem e) {
        return ServiceItemVO.builder()
                .id(e.getId()).tenantId(e.getTenantId()).schoolId(e.getSchoolId())
                .serviceOrgId(e.getServiceOrgId()).name(e.getName())
                .coverFileId(e.getCoverFileId()).type(e.getType()).status(e.getStatus())
                .createdAt(e.getCreatedAt()).updatedAt(e.getUpdatedAt())
                .build();
    }
}
