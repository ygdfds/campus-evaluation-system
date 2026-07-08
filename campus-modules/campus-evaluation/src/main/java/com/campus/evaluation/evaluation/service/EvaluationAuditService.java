package com.campus.evaluation.evaluation.service;

import com.campus.evaluation.common.core.domain.PageResult;
import com.campus.evaluation.evaluation.domain.dto.AuditDecisionDTO;
import com.campus.evaluation.evaluation.domain.vo.EvaluationAuditVO;
import com.campus.evaluation.evaluation.domain.vo.EvaluationFormDetailVO;
import com.campus.evaluation.evaluation.domain.vo.RiskItemVO;

import java.util.List;

public interface EvaluationAuditService {

    PageResult<EvaluationAuditVO> list(String keyword, String status, String formType,
                                       Long submitterId, int pageNum, int pageSize);

    /**
     * 审核详情（含表单信息 + 风险检查项）
     */
    AuditDetailResult getDetail(Long auditId);

    void approve(Long auditId);

    void reject(Long auditId, AuditDecisionDTO dto);

    /**
     * 审核详情复合返回
     */
    record AuditDetailResult(
            EvaluationFormDetailVO formDetail,
            EvaluationAuditVO audit,
            List<RiskItemVO> riskItems
    ) {}
}
