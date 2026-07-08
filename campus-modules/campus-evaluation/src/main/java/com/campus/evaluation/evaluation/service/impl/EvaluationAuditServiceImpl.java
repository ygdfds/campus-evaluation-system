package com.campus.evaluation.evaluation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.evaluation.common.core.domain.PageResult;
import com.campus.evaluation.common.core.exception.BusinessException;
import com.campus.evaluation.common.security.SecurityUtils;
import com.campus.evaluation.evaluation.domain.dto.AuditDecisionDTO;
import com.campus.evaluation.evaluation.domain.entity.EvaluationForm;
import com.campus.evaluation.evaluation.domain.entity.EvaluationFormPublishAudit;
import com.campus.evaluation.evaluation.domain.entity.EvaluationWindow;
import com.campus.evaluation.evaluation.domain.vo.*;
import com.campus.evaluation.evaluation.mapper.CrossModuleHelperMapper;
import com.campus.evaluation.evaluation.mapper.EvaluationFormMapper;
import com.campus.evaluation.evaluation.mapper.EvaluationFormPublishAuditMapper;
import com.campus.evaluation.evaluation.mapper.EvaluationWindowMapper;
import com.campus.evaluation.evaluation.service.EvaluationAuditService;
import com.campus.evaluation.evaluation.service.EvaluationFormService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EvaluationAuditServiceImpl implements EvaluationAuditService {

    private final EvaluationFormPublishAuditMapper auditMapper;
    private final EvaluationFormMapper formMapper;
    private final EvaluationWindowMapper windowMapper;
    private final CrossModuleHelperMapper helperMapper;
    private final EvaluationFormService formService;
    private final EvaluationFormServiceImpl formServiceImpl;

    @Override
    public PageResult<EvaluationAuditVO> list(String keyword, String status, String formType,
                                               Long submitterId, int pageNum, int pageSize) {
        Long tenantId = requireTenantId();

        LambdaQueryWrapper<EvaluationFormPublishAudit> wrapper = new LambdaQueryWrapper<EvaluationFormPublishAudit>()
                .eq(EvaluationFormPublishAudit::getTenantId, tenantId)
                .eq(status != null, EvaluationFormPublishAudit::getStatus, status)
                .eq(submitterId != null, EvaluationFormPublishAudit::getRequestedBy, submitterId)
                .orderByDesc(EvaluationFormPublishAudit::getCreatedAt);

        Page<EvaluationFormPublishAudit> page = auditMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);

        List<EvaluationAuditVO> records = page.getRecords().stream().map(a -> {
            EvaluationForm form = formMapper.selectById(a.getFormId());
            String title = form != null ? form.getTitle() : "";
            String type = form != null ? form.getType() : null;

            // keyword 过滤：基于表单标题
            if (keyword != null && !keyword.isEmpty() && !title.contains(keyword)) {
                return null;
            }
            // formType 过滤
            if (formType != null && !formType.equals(type)) {
                return null;
            }

            return EvaluationAuditVO.builder()
                    .id(a.getId())
                    .formId(a.getFormId())
                    .formTitle(title)
                    .status(a.getStatus())
                    .statusLabel(EvaluationFormServiceImpl.auditStatusLabel(a.getStatus()))
                    .submitterId(a.getRequestedBy())
                    .submitterName(helperMapper.selectRealNameByUserId(a.getRequestedBy()))
                    .submitterRole(a.getSubmitterRole())
                    .requestedAt(a.getRequestedAt())
                    .submitReason(a.getSubmitReason())
                    .reviewerId(a.getReviewedBy())
                    .reviewerName(a.getReviewedBy() != null ? helperMapper.selectRealNameByUserId(a.getReviewedBy()) : null)
                    .reviewedAt(a.getReviewedAt())
                    .reviewComment(a.getReviewComment())
                    .formType(type)
                    .formTypeLabel(EvaluationFormServiceImpl.formTypeLabel(type))
                    .build();
        }).filter(v -> v != null).collect(Collectors.toList());

        return new PageResult<>(page.getTotal(), records, pageNum, pageSize);
    }

    @Override
    public AuditDetailResult getDetail(Long auditId) {
        Long tenantId = requireTenantId();
        EvaluationFormPublishAudit audit = getAuditByIdAndTenant(auditId, tenantId);

        EvaluationFormDetailVO formDetail = formService.getDetail(audit.getFormId());
        EvaluationAuditVO auditVO = EvaluationAuditVO.builder()
                .id(audit.getId())
                .formId(audit.getFormId())
                .formTitle(formDetail.getTitle())
                .status(audit.getStatus())
                .statusLabel(EvaluationFormServiceImpl.auditStatusLabel(audit.getStatus()))
                .submitterId(audit.getRequestedBy())
                .submitterName(helperMapper.selectRealNameByUserId(audit.getRequestedBy()))
                .submitterRole(audit.getSubmitterRole())
                .requestedAt(audit.getRequestedAt())
                .reviewerId(audit.getReviewedBy())
                .reviewerName(audit.getReviewedBy() != null ? helperMapper.selectRealNameByUserId(audit.getReviewedBy()) : null)
                .reviewedAt(audit.getReviewedAt())
                .reviewComment(audit.getReviewComment())
                .formType(formDetail.getFormType())
                .formTypeLabel(formDetail.getFormTypeLabel())
                .build();

        // 风险检查
        EvaluationForm form = formMapper.selectOne(
                new LambdaQueryWrapper<EvaluationForm>()
                        .eq(EvaluationForm::getId, audit.getFormId())
                        .eq(EvaluationForm::getTenantId, tenantId));
        List<RiskItemVO> riskItems = formServiceImpl.performRiskCheck(form);

        return new AuditDetailResult(formDetail, auditVO, riskItems);
    }

    @Override
    @Transactional
    public void approve(Long auditId) {
        Long tenantId = requireTenantId();
        EvaluationFormPublishAudit audit = getAuditByIdAndTenant(auditId, tenantId);

        if (!"pending".equals(audit.getStatus())) {
            throw new BusinessException("该审核记录已处理");
        }

        EvaluationForm form = formMapper.selectOne(
                new LambdaQueryWrapper<EvaluationForm>()
                        .eq(EvaluationForm::getId, audit.getFormId())
                        .eq(EvaluationForm::getTenantId, tenantId));
        if (form == null) throw new BusinessException(404, "表单不存在");
        if (!"pending".equals(form.getStatus())) {
            throw new BusinessException("表单状态异常");
        }

        // 风险检查
        List<RiskItemVO> risks = formServiceImpl.performRiskCheck(form);
        boolean hasBlocker = risks.stream().anyMatch(r -> "blocker".equals(r.getLevel()));
        if (hasBlocker) {
            String msg = risks.stream().filter(r -> "blocker".equals(r.getLevel()))
                    .map(RiskItemVO::getMessage).collect(Collectors.joining("; "));
            throw new BusinessException("存在阻断项，无法通过：" + msg);
        }

        // 更新审核记录
        audit.setStatus("approved");
        audit.setReviewedBy(SecurityUtils.getUserId());
        audit.setReviewedAt(LocalDateTime.now());
        auditMapper.updateById(audit);

        // 更新表单状态
        form.setStatus("published");
        form.setPublishedAt(LocalDateTime.now());
        formMapper.updateById(form);

        // 更新窗口状态
        EvaluationWindow window = windowMapper.selectOne(
                new LambdaQueryWrapper<EvaluationWindow>()
                        .eq(EvaluationWindow::getFormId, form.getId())
                        .eq(EvaluationWindow::getTenantId, tenantId));
        if (window != null) {
            LocalDateTime now = LocalDateTime.now();
            if (now.isBefore(window.getStartAt())) {
                window.setStatus("scheduled");
            } else if (now.isAfter(window.getEndAt())) {
                window.setStatus("ended");
            } else {
                window.setStatus("active");
            }
            windowMapper.updateById(window);
        }
    }

    @Override
    @Transactional
    public void reject(Long auditId, AuditDecisionDTO dto) {
        Long tenantId = requireTenantId();
        EvaluationFormPublishAudit audit = getAuditByIdAndTenant(auditId, tenantId);

        if (!"pending".equals(audit.getStatus())) {
            throw new BusinessException("该审核记录已处理");
        }

        EvaluationForm form = formMapper.selectOne(
                new LambdaQueryWrapper<EvaluationForm>()
                        .eq(EvaluationForm::getId, audit.getFormId())
                        .eq(EvaluationForm::getTenantId, tenantId));
        if (form == null) throw new BusinessException(404, "表单不存在");

        // 更新审核记录
        audit.setStatus("rejected");
        audit.setReviewedBy(SecurityUtils.getUserId());
        audit.setReviewedAt(LocalDateTime.now());
        audit.setReviewComment(dto.getReason());
        auditMapper.updateById(audit);

        // 更新表单状态
        form.setStatus("rejected");
        formMapper.updateById(form);
    }

    private EvaluationFormPublishAudit getAuditByIdAndTenant(Long auditId, Long tenantId) {
        EvaluationFormPublishAudit audit = auditMapper.selectOne(
                new LambdaQueryWrapper<EvaluationFormPublishAudit>()
                        .eq(EvaluationFormPublishAudit::getId, auditId)
                        .eq(EvaluationFormPublishAudit::getTenantId, tenantId));
        if (audit == null) throw new BusinessException(404, "审核记录不存在");
        return audit;
    }

    private Long requireTenantId() {
        Long tenantId = SecurityUtils.getTenantId();
        if (tenantId == null) throw new BusinessException(403, "无法获取租户信息");
        return tenantId;
    }
}
