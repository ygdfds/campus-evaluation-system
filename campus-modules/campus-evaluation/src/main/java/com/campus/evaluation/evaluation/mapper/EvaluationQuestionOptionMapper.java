package com.campus.evaluation.evaluation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.evaluation.evaluation.domain.entity.EvaluationQuestionOption;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface EvaluationQuestionOptionMapper extends BaseMapper<EvaluationQuestionOption> {

    @Delete("DELETE FROM eval_question_option WHERE question_id IN " +
            "(SELECT id FROM eval_question WHERE form_id = #{formId})")
    int physicalDeleteByFormId(@Param("formId") Long formId);
}
