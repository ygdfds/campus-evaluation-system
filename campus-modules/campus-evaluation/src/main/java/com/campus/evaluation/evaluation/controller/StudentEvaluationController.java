package com.campus.evaluation.evaluation.controller;

import com.campus.evaluation.common.core.domain.R;
import com.campus.evaluation.evaluation.domain.dto.StudentEvaluationSubmitDTO;
import com.campus.evaluation.evaluation.domain.dto.StudentEvaluationUpdateDTO;
import com.campus.evaluation.evaluation.domain.vo.StudentEvaluationVO;
import com.campus.evaluation.evaluation.service.StudentEvaluationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/evaluation/student")
@RequiredArgsConstructor
public class StudentEvaluationController {

    private final StudentEvaluationService studentEvaluationService;

    @GetMapping("/tasks")
    public R<List<StudentEvaluationVO.Task>> listTasks() {
        return R.ok(studentEvaluationService.listTasks());
    }

    @GetMapping("/forms/{formId}/submit-page")
    public R<StudentEvaluationVO.SubmitPage> getSubmitPage(@PathVariable Long formId) {
        return R.ok(studentEvaluationService.getSubmitPage(formId));
    }

    @PostMapping("/submissions/draft")
    public R<StudentEvaluationVO.Submission> saveDraft(@Valid @RequestBody StudentEvaluationSubmitDTO dto) {
        return R.ok(studentEvaluationService.saveDraft(dto));
    }

    @PostMapping("/submissions/submit")
    public R<StudentEvaluationVO.Submission> submit(@Valid @RequestBody StudentEvaluationSubmitDTO dto) {
        return R.ok(studentEvaluationService.submit(dto));
    }

    @PutMapping("/submissions/{id}")
    public R<StudentEvaluationVO.Submission> updateSubmitted(@PathVariable Long id,
                                                             @Valid @RequestBody StudentEvaluationUpdateDTO dto) {
        return R.ok(studentEvaluationService.updateSubmitted(id, dto));
    }

    @GetMapping("/submissions/my")
    public R<List<StudentEvaluationVO.History>> listMySubmissions() {
        return R.ok(studentEvaluationService.listMySubmissions());
    }

    @GetMapping("/courses")
    public R<List<StudentEvaluationVO.SimpleOption>> listCourses() {
        return R.ok(studentEvaluationService.listCourses());
    }

    @GetMapping("/course-enrollments")
    public R<List<StudentEvaluationVO.SimpleOption>> listCourseEnrollments() {
        return R.ok(studentEvaluationService.listCourseEnrollments());
    }

    @GetMapping("/service-items")
    public R<List<StudentEvaluationVO.SimpleOption>> listServiceItems() {
        return R.ok(studentEvaluationService.listServiceItems());
    }

    @GetMapping("/teaching-orgs")
    public R<List<StudentEvaluationVO.SimpleOption>> listTeachingOrgs() {
        return R.ok(studentEvaluationService.listTeachingOrgs());
    }
}
