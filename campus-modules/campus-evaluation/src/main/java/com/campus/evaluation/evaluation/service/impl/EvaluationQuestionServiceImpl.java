package com.campus.evaluation.evaluation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.evaluation.common.core.exception.BusinessException;
import com.campus.evaluation.common.security.SecurityUtils;
import com.campus.evaluation.evaluation.domain.dto.EvaluationQuestionSaveDTO;
import com.campus.evaluation.evaluation.domain.entity.EvaluationForm;
import com.campus.evaluation.evaluation.domain.entity.EvaluationQuestion;
import com.campus.evaluation.evaluation.domain.entity.EvaluationQuestionOption;
import com.campus.evaluation.evaluation.domain.vo.EvaluationQuestionVO;
import com.campus.evaluation.evaluation.mapper.CrossModuleHelperMapper;
import com.campus.evaluation.evaluation.mapper.EvaluationFormMapper;
import com.campus.evaluation.evaluation.mapper.EvaluationQuestionMapper;
import com.campus.evaluation.evaluation.mapper.EvaluationQuestionOptionMapper;
import com.campus.evaluation.evaluation.service.EvaluationQuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EvaluationQuestionServiceImpl implements EvaluationQuestionService {

    private final EvaluationFormMapper formMapper;
    private final EvaluationQuestionMapper questionMapper;
    private final EvaluationQuestionOptionMapper optionMapper;

    @Override
    public List<EvaluationQuestionVO> getQuestions(Long formId) {
        Long tenantId = requireTenantId();
        verifyFormAccess(formId, tenantId);

        List<EvaluationQuestion> questions = questionMapper.selectList(
                new LambdaQueryWrapper<EvaluationQuestion>()
                        .eq(EvaluationQuestion::getFormId, formId)
                        .eq(EvaluationQuestion::getTenantId, tenantId)
                        .orderByAsc(EvaluationQuestion::getSortOrder));

        return questions.stream().map(q -> {
            List<EvaluationQuestionOption> options = optionMapper.selectList(
                    new LambdaQueryWrapper<EvaluationQuestionOption>()
                            .eq(EvaluationQuestionOption::getQuestionId, q.getId())
                            .eq(EvaluationQuestionOption::getTenantId, tenantId)
                            .orderByAsc(EvaluationQuestionOption::getSortOrder));

            return EvaluationQuestionVO.builder()
                    .id(q.getId())
                    .title(q.getTitle())
                    .questionType(q.getType())
                    .questionTypeLabel(questionTypeLabel(q.getType()))
                    .required(q.getRequired())
                    .sortOrder(q.getSortOrder())
                    .maxScore(q.getMaxScore())
                    .options(options.stream().map(o -> EvaluationQuestionVO.OptionVO.builder()
                            .id(o.getId())
                            .label(o.getOptionText())
                            .sortOrder(o.getSortOrder())
                            .build()).collect(Collectors.toList()))
                    .build();
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void saveQuestions(Long formId, EvaluationQuestionSaveDTO dto) {
        Long tenantId = requireTenantId();
        EvaluationForm form = verifyFormAccess(formId, tenantId);
        assertEditable(form);

        // 校验题目
        validateQuestions(dto);

        // 物理删除旧选项和旧题目
        optionMapper.physicalDeleteByFormId(formId);
        questionMapper.physicalDeleteByFormId(formId);

        // 插入新题目和选项
        Long schoolId = SecurityUtils.getSchoolId();
        int order = 0;
        for (EvaluationQuestionSaveDTO.QuestionItem item : dto.getQuestions()) {
            order++;
            EvaluationQuestion q = new EvaluationQuestion();
            q.setTenantId(tenantId);
            q.setSchoolId(schoolId);
            q.setFormId(formId);
            q.setType(item.getQuestionType());
            q.setTitle(item.getTitle());
            q.setRequired(item.getRequired() != null ? item.getRequired() : true);
            q.setSortOrder(item.getSortOrder() != null ? item.getSortOrder() : order);
            if ("rating".equals(item.getQuestionType())) {
                int maxScore = item.getMaxScore() != null ? item.getMaxScore() : 5;
                if (maxScore < 1 || maxScore > 10) maxScore = 5;
                q.setMaxScore(BigDecimal.valueOf(maxScore));
            }
            questionMapper.insert(q);

            if (item.getOptions() != null && !item.getOptions().isEmpty()) {
                int optOrder = 0;
                for (EvaluationQuestionSaveDTO.OptionItem opt : item.getOptions()) {
                    optOrder++;
                    EvaluationQuestionOption option = new EvaluationQuestionOption();
                    option.setTenantId(tenantId);
                    option.setSchoolId(schoolId);
                    option.setQuestionId(q.getId());
                    option.setOptionText(opt.getLabel());
                    option.setSortOrder(opt.getSortOrder() != null ? opt.getSortOrder() : optOrder);
                    optionMapper.insert(option);
                }
            }
        }
    }

    private void validateQuestions(EvaluationQuestionSaveDTO dto) {
        for (EvaluationQuestionSaveDTO.QuestionItem q : dto.getQuestions()) {
            String type = q.getQuestionType();
            if (!List.of("rating", "single", "multiple", "text").contains(type)) {
                throw new BusinessException("题目类型必须为 rating/single/multiple/text");
            }
            if (("single".equals(type) || "multiple".equals(type))) {
                if (q.getOptions() == null || q.getOptions().size() < 2) {
                    throw new BusinessException("单选/多选题必须至少 2 个选项");
                }
            }
            if ("rating".equals(type) && q.getMaxScore() != null) {
                if (q.getMaxScore() < 1 || q.getMaxScore() > 10) {
                    throw new BusinessException("评分题最大分值需在 1-10 之间");
                }
            }
        }
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
            throw new BusinessException("仅草稿或已驳回状态的表单可以编辑题目");
        }
    }

    static String questionTypeLabel(String type) {
        if (type == null) return null;
        return switch (type) {
            case "rating" -> "评分题";
            case "single" -> "单选题";
            case "multiple" -> "多选题";
            case "text" -> "文本题";
            default -> type;
        };
    }

    private Long requireTenantId() {
        Long tenantId = SecurityUtils.getTenantId();
        if (tenantId == null) throw new BusinessException(403, "无法获取租户信息");
        return tenantId;
    }
}
