package com.campus.evaluation.evaluation.service;

import com.campus.evaluation.evaluation.domain.dto.StudentEvaluationSubmitDTO;
import com.campus.evaluation.evaluation.domain.dto.StudentEvaluationUpdateDTO;
import com.campus.evaluation.evaluation.domain.vo.StudentEvaluationVO;

import java.util.List;

public interface StudentEvaluationService {

    List<StudentEvaluationVO.Task> listTasks();

    StudentEvaluationVO.SubmitPage getSubmitPage(Long formId);

    StudentEvaluationVO.Submission saveDraft(StudentEvaluationSubmitDTO dto);

    StudentEvaluationVO.Submission submit(StudentEvaluationSubmitDTO dto);

    StudentEvaluationVO.Submission updateSubmitted(Long submissionId, StudentEvaluationUpdateDTO dto);

    List<StudentEvaluationVO.History> listMySubmissions();

    List<StudentEvaluationVO.SimpleOption> listCourses();

    List<StudentEvaluationVO.SimpleOption> listCourseEnrollments();

    List<StudentEvaluationVO.SimpleOption> listServiceItems();

    List<StudentEvaluationVO.SimpleOption> listTeachingOrgs();
}
