package com.campus.evaluation.evaluation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.evaluation.common.core.exception.BusinessException;
import com.campus.evaluation.common.security.SecurityUtils;
import com.campus.evaluation.evaluation.domain.dto.EvaluationWindowSaveDTO;
import com.campus.evaluation.evaluation.domain.entity.EvaluationForm;
import com.campus.evaluation.evaluation.domain.entity.EvaluationWindow;
import com.campus.evaluation.evaluation.domain.vo.EvaluationWindowVO;
import com.campus.evaluation.evaluation.mapper.EvaluationFormMapper;
import com.campus.evaluation.evaluation.mapper.EvaluationWindowMapper;
import com.campus.evaluation.evaluation.service.EvaluationWindowService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EvaluationWindowServiceImpl implements EvaluationWindowService {

    private final EvaluationFormMapper formMapper;
    private final EvaluationWindowMapper windowMapper;

    @Override
    public EvaluationWindowVO getWindow(Long formId) {
        Long tenantId = requireTenantId();
        verifyFormAccess(formId, tenantId);

        EvaluationWindow window = windowMapper.selectOne(
                new LambdaQueryWrapper<EvaluationWindow>()
                        .eq(EvaluationWindow::getFormId, formId)
                        .eq(EvaluationWindow::getTenantId, tenantId));
        if (window == null) throw new BusinessException(404, "窗口未配置");
        return toVO(window);
    }

    @Override
    public void saveWindow(Long formId, EvaluationWindowSaveDTO dto) {
        Long tenantId = requireTenantId();
        EvaluationForm form = verifyFormAccess(formId, tenantId);
        assertEditable(form);

        // 校验
        if (!dto.getEndTime().isAfter(dto.getStartTime())) {
            throw new BusinessException("结束时间必须晚于开始时间");
        }

        // 查找已有窗口
        EvaluationWindow window = windowMapper.selectOne(
                new LambdaQueryWrapper<EvaluationWindow>()
                        .eq(EvaluationWindow::getFormId, formId)
                        .eq(EvaluationWindow::getTenantId, tenantId));

        if (window == null) {
            window = new EvaluationWindow();
            window.setTenantId(tenantId);
            window.setSchoolId(SecurityUtils.getSchoolId());
            window.setFormId(formId);
            window.setType("evaluation");
            window.setModifiableHours(24);
            window.setStatus("scheduled");
        }
        window.setStartAt(dto.getStartTime());
        window.setEndAt(dto.getEndTime());
        windowMapper.insertOrUpdate(window);
    }

    private EvaluationForm verifyFormAccess(Long formId, Long tenantId) {
        EvaluationForm form = formMapper.selectOne(
                new LambdaQueryWrapper<EvaluationForm>()
                        .eq(EvaluationForm::getId, formId)
                        .eq(EvaluationForm::getTenantId, tenantId));
        if (form == null) throw new BusinessException(404, "表单不存在");
        return form;
    }

    private void assertEditable(EvaluationForm form) {
        if (!"draft".equals(form.getStatus()) && !"rejected".equals(form.getStatus())) {
            throw new BusinessException("仅草稿或已驳回状态的表单可以编辑窗口");
        }
    }

    private EvaluationWindowVO toVO(EvaluationWindow w) {
        return EvaluationWindowVO.builder()
                .id(w.getId())
                .startTime(w.getStartAt())
                .endTime(w.getEndAt())
                .status(w.getStatus())
                .statusLabel(windowStatusLabel(w.getStatus()))
                .build();
    }

    static String windowStatusLabel(String status) {
        if (status == null) return null;
        return switch (status) {
            case "scheduled" -> "未开始";
            case "active" -> "进行中";
            case "ended" -> "已结束";
            case "closed" -> "已关闭";
            default -> status;
        };
    }

    private Long requireTenantId() {
        Long tenantId = SecurityUtils.getTenantId();
        if (tenantId == null) throw new BusinessException(403, "无法获取租户信息");
        return tenantId;
    }
}
