package com.campus.evaluation.evaluation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.evaluation.evaluation.domain.entity.EvaluationQuestion;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface EvaluationQuestionMapper extends BaseMapper<EvaluationQuestion> {

    @Delete("DELETE FROM eval_question WHERE form_id = #{formId}")
    int physicalDeleteByFormId(@Param("formId") Long formId);
}
