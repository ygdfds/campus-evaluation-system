package com.campus.evaluation.evaluation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.evaluation.evaluation.domain.entity.EvaluationForm;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface EvaluationFormMapper extends BaseMapper<EvaluationForm> {

    @Select("SELECT COUNT(*) FROM eval_question WHERE form_id = #{formId} AND deleted = 0")
    int countQuestions(@Param("formId") Long formId);
}
