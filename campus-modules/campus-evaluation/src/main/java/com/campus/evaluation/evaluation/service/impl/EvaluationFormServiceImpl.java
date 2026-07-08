package com.campus.evaluation.evaluation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.evaluation.common.core.domain.PageResult;
import com.campus.evaluation.common.core.exception.BusinessException;
import com.campus.evaluation.common.security.SecurityUtils;
import com.campus.evaluation.evaluation.domain.dto.EvaluationFormCreateDTO;
import com.campus.evaluation.evaluation.domain.dto.EvaluationFormUpdateDTO;
import com.campus.evaluation.evaluation.domain.entity.*;
import com.campus.evaluation.evaluation.domain.vo.*;
import com.campus.evaluation.evaluation.mapper.*;
import com.campus.evaluation.evaluation.service.EvaluationFormService;
import com.campus.evaluation.evaluation.service.EvaluationQuestionService;
import com.campus.evaluation.evaluation.service.EvaluationWindowService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EvaluationFormServiceImpl implements EvaluationFormService {

    private final EvaluationFormMapper formMapper;
    private final EvaluationQuestionMapper questionMapper;
    private final EvaluationQuestionOptionMapper optionMapper;
    private final EvaluationWindowMapper windowMapper;
    private final EvaluationFormPublishAuditMapper auditMapper;
    private final CrossModuleHelperMapper helperMapper;
    private final EvaluationQuestionService questionService;
    private final EvaluationWindowService windowService;

    @Override
    public PageResult<EvaluationFormListVO> list(String keyword, String status, String formType,
                                                  String targetType, Long creatorId,
                                                  int pageNum, int pageSize) {
        Long tenantId = requireTenantId();

        LambdaQueryWrapper<EvaluationForm> wrapper = new LambdaQueryWrapper<EvaluationForm>()
                .eq(EvaluationForm::getTenantId, tenantId)
                .like(keyword != null && !keyword.isEmpty(), EvaluationForm::getTitle, keyword)
                .eq(status != null, EvaluationForm::getStatus, status)
                .eq(formType != null, EvaluationForm::getType, formType)
                .eq(creatorId != null, EvaluationForm::getPublisherId, creatorId);

        // targetType 过滤: course -> courseId IS NOT NULL, service_item -> serviceItemId IS NOT NULL
        if ("course".equals(targetType)) {
            wrapper.isNotNull(EvaluationForm::getCourseId);
        } else if ("service_item".equals(targetType)) {
            wrapper.isNotNull(EvaluationForm::getServiceItemId);
        }

        wrapper.orderByDesc(EvaluationForm::getCreatedAt);
        Page<EvaluationForm> page = formMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);

        List<EvaluationFormListVO> records = page.getRecords().stream()
                .map(this::toListVO)
                .collect(Collectors.toList());

        return new PageResult<>(page.getTotal(), records, pageNum, pageSize);
    }

    @Override
    public EvaluationFormDetailVO getDetail(Long id) {
        Long tenantId = requireTenantId();
        EvaluationForm form = getFormByIdAndTenant(id, tenantId);

        // 题目
        List<EvaluationQuestionVO> questions = questionService.getQuestions(id);

        // 窗口
        EvaluationWindowVO window = null;
        try {
            window = windowService.getWindow(id);
        } catch (BusinessException ignored) {
            // 窗口可能未配置
        }

        // 审核记录
        List<EvaluationFormPublishAudit> audits = auditMapper.selectList(
                new LambdaQueryWrapper<EvaluationFormPublishAudit>()
                        .eq(EvaluationFormPublishAudit::getFormId, id)
                        .eq(EvaluationFormPublishAudit::getTenantId, tenantId)
                        .orderByDesc(EvaluationFormPublishAudit::getCreatedAt));
        List<EvaluationAuditVO> auditRecords = audits.stream()
                .map(a -> toAuditVO(a, form.getTitle())).collect(Collectors.toList());

        // 评价对象名称
        String targetName = resolveTargetName(form);
        String targetType = resolveTargetType(form);
        Long targetId = resolveTargetId(form);

        return EvaluationFormDetailVO.builder()
                .id(form.getId())
                .title(form.getTitle())
                .description(form.getDescription())
                .formType(form.getType())
                .formTypeLabel(formTypeLabel(form.getType()))
                .targetType(targetType)
                .targetTypeLabel(targetTypeLabel(targetType))
                .targetId(targetId)
                .targetName(targetName)
                .status(form.getStatus())
                .statusLabel(statusLabel(form.getStatus()))
                .anonymousEnabled(form.getAnonymous())
                .scoreEnabled(form.getScoreEnabled())
                .creatorId(form.getPublisherId())
                .creatorName(helperMapper.selectRealNameByUserId(form.getPublisherId()))
                .coverFileId(form.getCoverFileId())
                .publishedAt(form.getPublishedAt())
                .createdAt(form.getCreatedAt())
                .updatedAt(form.getUpdatedAt())
                .questions(questions)
                .window(window)
                .auditRecords(auditRecords)
                .build();
    }

    @Override
    public EvaluationFormListVO create(EvaluationFormCreateDTO dto) {
        Long tenantId = requireTenantId();

        // 校验 targetType
        validateTargetType(dto.getTargetType());
        validateTargetExists(tenantId, dto.getTargetType(), dto.getTargetId());

        EvaluationForm form = new EvaluationForm();
        form.setTenantId(tenantId);
        form.setSchoolId(SecurityUtils.getSchoolId());
        form.setType(dto.getFormType());
        form.setTitle(dto.getTitle());
        form.setDescription(dto.getDescription());
        form.setCoverFileId(dto.getCoverFileId());
        form.setPublisherId(SecurityUtils.getUserId());
        form.setPublishScope("school");
        form.setAnonymous(dto.getAnonymousEnabled() != null ? dto.getAnonymousEnabled() : true);
        form.setScoreEnabled(dto.getScoreEnabled() != null ? dto.getScoreEnabled() : false);
        form.setStatus("draft");

        // 设置评价对象
        if ("course".equals(dto.getTargetType())) {
            form.setCourseId(dto.getTargetId());
        } else if ("service_item".equals(dto.getTargetType())) {
            form.setServiceItemId(dto.getTargetId());
        }

        formMapper.insert(form);
        return toListVO(form);
    }

    @Override
    public EvaluationFormListVO update(Long id, EvaluationFormUpdateDTO dto) {
        Long tenantId = requireTenantId();
        EvaluationForm form = getFormByIdAndTenant(id, tenantId);
        assertEditable(form);

        validateTargetType(dto.getTargetType());
        validateTargetExists(tenantId, dto.getTargetType(), dto.getTargetId());

        form.setType(dto.getFormType());
        form.setTitle(dto.getTitle());
        form.setDescription(dto.getDescription());
        form.setCoverFileId(dto.getCoverFileId());
        form.setAnonymous(dto.getAnonymousEnabled() != null ? dto.getAnonymousEnabled() : form.getAnonymous());
        form.setScoreEnabled(dto.getScoreEnabled() != null ? dto.getScoreEnabled() : form.getScoreEnabled());

        // 重设评价对象
        form.setCourseId(null);
        form.setServiceItemId(null);
        if ("course".equals(dto.getTargetType())) {
            form.setCourseId(dto.getTargetId());
        } else if ("service_item".equals(dto.getTargetType())) {
            form.setServiceItemId(dto.getTargetId());
        }

        formMapper.updateById(form);
        return toListVO(form);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Long tenantId = requireTenantId();
        EvaluationForm form = getFormByIdAndTenant(id, tenantId);
        if (!"draft".equals(form.getStatus()) && !"rejected".equals(form.getStatus())) {
            throw new BusinessException("仅草稿或已驳回的表单可以删除");
        }
        // 软删除关联数据
        questionMapper.physicalDeleteByFormId(id);
        optionMapper.physicalDeleteByFormId(id);
        windowMapper.delete(new LambdaQueryWrapper<EvaluationWindow>()
                .eq(EvaluationWindow::getFormId, id));
        formMapper.deleteById(id);
    }

    @Override
    @Transactional
    public EvaluationFormListVO copy(Long id) {
        Long tenantId = requireTenantId();
        EvaluationForm source = getFormByIdAndTenant(id, tenantId);

        // 复制表单基础信息
        EvaluationForm copy = new EvaluationForm();
        copy.setTenantId(tenantId);
        copy.setSchoolId(SecurityUtils.getSchoolId());
        copy.setType(source.getType());
        copy.setTitle(source.getTitle() + " 副本");
        copy.setDescription(source.getDescription());
        copy.setCoverFileId(source.getCoverFileId());
        copy.setPublisherId(SecurityUtils.getUserId());
        copy.setPublishScope(source.getPublishScope());
        copy.setAnonymous(source.getAnonymous());
        copy.setScoreEnabled(source.getScoreEnabled());
        copy.setTeachingOrgId(source.getTeachingOrgId());
        copy.setServiceOrgId(source.getServiceOrgId());
        copy.setCourseId(source.getCourseId());
        copy.setServiceItemId(source.getServiceItemId());
        copy.setStatus("draft");
        formMapper.insert(copy);

        // 复制题目和选项
        List<EvaluationQuestion> questions = questionMapper.selectList(
                new LambdaQueryWrapper<EvaluationQuestion>()
                        .eq(EvaluationQuestion::getFormId, id)
                        .eq(EvaluationQuestion::getTenantId, tenantId)
                        .orderByAsc(EvaluationQuestion::getSortOrder));
        for (EvaluationQuestion q : questions) {
            List<EvaluationQuestionOption> options = optionMapper.selectList(
                    new LambdaQueryWrapper<EvaluationQuestionOption>()
                            .eq(EvaluationQuestionOption::getQuestionId, q.getId())
                            .eq(EvaluationQuestionOption::getTenantId, tenantId)
                            .orderByAsc(EvaluationQuestionOption::getSortOrder));

            Long oldQId = q.getId();
            q.setId(null);
            q.setFormId(copy.getId());
            q.setSchoolId(SecurityUtils.getSchoolId());
            questionMapper.insert(q);

            for (EvaluationQuestionOption opt : options) {
                opt.setId(null);
                opt.setQuestionId(q.getId());
                opt.setSchoolId(SecurityUtils.getSchoolId());
                optionMapper.insert(opt);
            }
        }

        // 复制窗口
        EvaluationWindow window = windowMapper.selectOne(
                new LambdaQueryWrapper<EvaluationWindow>()
                        .eq(EvaluationWindow::getFormId, id)
                        .eq(EvaluationWindow::getTenantId, tenantId));
        if (window != null) {
            window.setId(null);
            window.setFormId(copy.getId());
            window.setSchoolId(SecurityUtils.getSchoolId());
            window.setStatus("scheduled");
            windowMapper.insert(window);
        }

        return toListVO(copy);
    }

    @Override
    @Transactional
    public void submitAudit(Long id) {
        Long tenantId = requireTenantId();
        EvaluationForm form = getFormByIdAndTenant(id, tenantId);
        if (!"draft".equals(form.getStatus()) && !"rejected".equals(form.getStatus())) {
            throw new BusinessException("仅草稿或已驳回的表单可以提交审核");
        }

        // 风险检查
        List<RiskItemVO> risks = performRiskCheck(form);
        boolean hasBlocker = risks.stream().anyMatch(r -> "blocker".equals(r.getLevel()));
        if (hasBlocker) {
            String msg = risks.stream().filter(r -> "blocker".equals(r.getLevel()))
                    .map(RiskItemVO::getMessage).collect(Collectors.joining("; "));
            throw new BusinessException("存在阻断项，无法提交审核：" + msg);
        }

        // 更新表单状态
        form.setStatus("pending");
        formMapper.updateById(form);

        // 创建审核记录
        EvaluationFormPublishAudit audit = new EvaluationFormPublishAudit();
        audit.setTenantId(tenantId);
        audit.setSchoolId(SecurityUtils.getSchoolId());
        audit.setFormId(id);
        audit.setAction("publish");
        audit.setStatus("pending");
        audit.setRequestedBy(SecurityUtils.getUserId());
        audit.setRequestedAt(LocalDateTime.now());
        audit.setSubmitterRole(SecurityUtils.getRoles().stream().findFirst().orElse(""));
        auditMapper.insert(audit);
    }

    // ========== 辅助方法 ==========

    private EvaluationForm getFormByIdAndTenant(Long id, Long tenantId) {
        EvaluationForm form = formMapper.selectOne(
                new LambdaQueryWrapper<EvaluationForm>()
                        .eq(EvaluationForm::getId, id)
                        .eq(EvaluationForm::getTenantId, tenantId));
        if (form == null) throw new BusinessException(404, "表单不存在");
        return form;
    }

    private void assertEditable(EvaluationForm form) {
        if (!"draft".equals(form.getStatus()) && !"rejected".equals(form.getStatus())) {
            throw new BusinessException("仅草稿或已驳回状态的表单可以编辑");
        }
    }

    private void validateTargetType(String targetType) {
        if (!"course".equals(targetType) && !"service_item".equals(targetType)) {
            throw new BusinessException("评价对象类型必须是 course 或 service_item");
        }
    }

    private void validateTargetExists(Long tenantId, String targetType, Long targetId) {
        if ("course".equals(targetType)) {
            if (helperMapper.countCourseByIdAndTenant(targetId, tenantId) == 0) {
                throw new BusinessException(404, "课程不存在");
            }
        } else if ("service_item".equals(targetType)) {
            if (helperMapper.countServiceItemByIdAndTenant(targetId, tenantId) == 0) {
                throw new BusinessException(404, "服务项目不存在");
            }
        }
    }

    private EvaluationFormListVO toListVO(EvaluationForm form) {
        String targetType = resolveTargetType(form);
        String targetName = resolveTargetName(form);
        int questionCount = formMapper.countQuestions(form.getId());

        // 获取窗口信息
        EvaluationWindow window = windowMapper.selectOne(
                new LambdaQueryWrapper<EvaluationWindow>()
                        .eq(EvaluationWindow::getFormId, form.getId())
                        .eq(EvaluationWindow::getTenantId, form.getTenantId()));

        // 获取最新审核状态
        EvaluationFormPublishAudit latestAudit = auditMapper.selectOne(
                new LambdaQueryWrapper<EvaluationFormPublishAudit>()
                        .eq(EvaluationFormPublishAudit::getFormId, form.getId())
                        .eq(EvaluationFormPublishAudit::getTenantId, form.getTenantId())
                        .orderByDesc(EvaluationFormPublishAudit::getCreatedAt)
                        .last("LIMIT 1"));

        return EvaluationFormListVO.builder()
                .id(form.getId())
                .title(form.getTitle())
                .formType(form.getType())
                .formTypeLabel(formTypeLabel(form.getType()))
                .targetType(targetType)
                .targetTypeLabel(targetTypeLabel(targetType))
                .targetName(targetName)
                .status(form.getStatus())
                .statusLabel(statusLabel(form.getStatus()))
                .creatorId(form.getPublisherId())
                .creatorName(helperMapper.selectRealNameByUserId(form.getPublisherId()))
                .questionCount(questionCount)
                .windowStartTime(window != null ? window.getStartAt() : null)
                .windowEndTime(window != null ? window.getEndAt() : null)
                .auditStatus(latestAudit != null ? latestAudit.getStatus() : null)
                .coverFileId(form.getCoverFileId())
                .createdAt(form.getCreatedAt())
                .updatedAt(form.getUpdatedAt())
                .build();
    }

    private EvaluationAuditVO toAuditVO(EvaluationFormPublishAudit a, String formTitle) {
        return EvaluationAuditVO.builder()
                .id(a.getId())
                .formId(a.getFormId())
                .formTitle(formTitle)
                .status(a.getStatus())
                .statusLabel(auditStatusLabel(a.getStatus()))
                .submitterId(a.getRequestedBy())
                .submitterName(helperMapper.selectRealNameByUserId(a.getRequestedBy()))
                .submitterRole(a.getSubmitterRole())
                .requestedAt(a.getRequestedAt())
                .submitReason(a.getSubmitReason())
                .reviewerId(a.getReviewedBy())
                .reviewerName(a.getReviewedBy() != null ? helperMapper.selectRealNameByUserId(a.getReviewedBy()) : null)
                .reviewedAt(a.getReviewedAt())
                .reviewComment(a.getReviewComment())
                .build();
    }

    private String resolveTargetType(EvaluationForm form) {
        if (form.getCourseId() != null) return "course";
        if (form.getServiceItemId() != null) return "service_item";
        return null;
    }

    private Long resolveTargetId(EvaluationForm form) {
        if (form.getCourseId() != null) return form.getCourseId();
        if (form.getServiceItemId() != null) return form.getServiceItemId();
        return null;
    }

    private String resolveTargetName(EvaluationForm form) {
        if (form.getCourseId() != null) {
            String name = helperMapper.selectCourseNameById(form.getCourseId());
            return name != null ? name : "对象不存在";
        }
        if (form.getServiceItemId() != null) {
            String name = helperMapper.selectServiceItemNameById(form.getServiceItemId());
            return name != null ? name : "对象不存在";
        }
        return null;
    }

    /**
     * 风险检查（提交审核 & 审核通过时复用）
     */
    public List<RiskItemVO> performRiskCheck(EvaluationForm form) {
        List<RiskItemVO> items = new ArrayList<>();

        // Blocker 检查
        if (form.getTitle() == null || form.getTitle().isBlank()) {
            items.add(RiskItemVO.builder().level("blocker").message("表单标题为空").build());
        }
        if (form.getCourseId() == null && form.getServiceItemId() == null) {
            items.add(RiskItemVO.builder().level("blocker").message("评价对象未配置").build());
        } else {
            String targetName = resolveTargetName(form);
            if ("对象不存在".equals(targetName)) {
                items.add(RiskItemVO.builder().level("blocker").message("评价对象不存在").build());
            }
        }
        int questionCount = formMapper.countQuestions(form.getId());
        if (questionCount == 0) {
            items.add(RiskItemVO.builder().level("blocker").message("评价题目未配置").build());
        }

        EvaluationWindow window = windowMapper.selectOne(
                new LambdaQueryWrapper<EvaluationWindow>()
                        .eq(EvaluationWindow::getFormId, form.getId())
                        .eq(EvaluationWindow::getTenantId, form.getTenantId()));
        if (window == null) {
            items.add(RiskItemVO.builder().level("blocker").message("评价窗口未配置").build());
        } else {
            if (window.getEndAt() != null && window.getStartAt() != null
                    && !window.getEndAt().isAfter(window.getStartAt())) {
                items.add(RiskItemVO.builder().level("blocker").message("窗口结束时间必须晚于开始时间").build());
            }
        }

        // Warning 检查
        if (window != null && window.getStartAt() != null && window.getStartAt().isBefore(LocalDateTime.now())) {
            items.add(RiskItemVO.builder().level("warning").message("评价窗口开始时间早于当前时间").build());
        }
        if (form.getDescription() == null || form.getDescription().isBlank()) {
            items.add(RiskItemVO.builder().level("warning").message("表单说明为空").build());
        }
        if (questionCount > 0) {
            long ratingCount = questionMapper.selectCount(
                    new LambdaQueryWrapper<EvaluationQuestion>()
                            .eq(EvaluationQuestion::getFormId, form.getId())
                            .eq(EvaluationQuestion::getType, "rating"));
            if (ratingCount == 0) {
                items.add(RiskItemVO.builder().level("warning").message("题目中没有评分题").build());
            }
        }

        return items;
    }

    static String formTypeLabel(String type) {
        if (type == null) return null;
        return switch (type) {
            case "course_evaluation" -> "课程评价";
            case "service_evaluation" -> "服务评价";
            default -> type;
        };
    }

    static String targetTypeLabel(String type) {
        if (type == null) return null;
        return switch (type) {
            case "course" -> "课程";
            case "service_item" -> "服务项目";
            default -> type;
        };
    }

    static String statusLabel(String status) {
        if (status == null) return null;
        return switch (status) {
            case "draft" -> "草稿";
            case "pending" -> "待审核";
            case "published" -> "已发布";
            case "rejected" -> "已驳回";
            case "closed" -> "已关闭";
            default -> status;
        };
    }

    static String auditStatusLabel(String status) {
        if (status == null) return null;
        return switch (status) {
            case "pending" -> "待审核";
            case "approved" -> "已通过";
            case "rejected" -> "已驳回";
            default -> status;
        };
    }

    private Long requireTenantId() {
        Long tenantId = SecurityUtils.getTenantId();
        if (tenantId == null) throw new BusinessException(403, "无法获取租户信息");
        return tenantId;
    }
}
