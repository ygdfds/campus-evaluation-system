package com.campus.evaluation.evaluation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.evaluation.common.core.exception.BusinessException;
import com.campus.evaluation.common.security.SecurityUtils;
import com.campus.evaluation.evaluation.domain.dto.StudentEvaluationSubmitDTO;
import com.campus.evaluation.evaluation.domain.dto.StudentEvaluationUpdateDTO;
import com.campus.evaluation.evaluation.domain.entity.EvaluationAnswer;
import com.campus.evaluation.evaluation.domain.entity.EvaluationAttachment;
import com.campus.evaluation.evaluation.domain.entity.EvaluationForm;
import com.campus.evaluation.evaluation.domain.entity.EvaluationQuestion;
import com.campus.evaluation.evaluation.domain.entity.EvaluationQuestionOption;
import com.campus.evaluation.evaluation.domain.entity.EvaluationScore;
import com.campus.evaluation.evaluation.domain.entity.EvaluationSubmission;
import com.campus.evaluation.evaluation.domain.entity.EvaluationWindow;
import com.campus.evaluation.evaluation.domain.vo.StudentEvaluationVO;
import com.campus.evaluation.evaluation.mapper.CrossModuleHelperMapper;
import com.campus.evaluation.evaluation.mapper.EvaluationAnswerMapper;
import com.campus.evaluation.evaluation.mapper.EvaluationAttachmentMapper;
import com.campus.evaluation.evaluation.mapper.EvaluationFormMapper;
import com.campus.evaluation.evaluation.mapper.EvaluationQuestionMapper;
import com.campus.evaluation.evaluation.mapper.EvaluationQuestionOptionMapper;
import com.campus.evaluation.evaluation.mapper.EvaluationScoreMapper;
import com.campus.evaluation.evaluation.mapper.EvaluationSubmissionMapper;
import com.campus.evaluation.evaluation.mapper.EvaluationWindowMapper;
import com.campus.evaluation.evaluation.service.StudentEvaluationService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentEvaluationServiceImpl implements StudentEvaluationService {

    private static final DateTimeFormatter SHORT_DATE = DateTimeFormatter.ofPattern("MM-dd");

    private final EvaluationFormMapper formMapper;
    private final EvaluationWindowMapper windowMapper;
    private final EvaluationQuestionMapper questionMapper;
    private final EvaluationQuestionOptionMapper optionMapper;
    private final EvaluationSubmissionMapper submissionMapper;
    private final EvaluationAnswerMapper answerMapper;
    private final EvaluationScoreMapper scoreMapper;
    private final EvaluationAttachmentMapper attachmentMapper;
    private final CrossModuleHelperMapper helperMapper;
    private final ObjectMapper objectMapper;

    @Override
    public List<StudentEvaluationVO.Task> listTasks() {
        Long tenantId = requireTenantId();
        Long userId = requireUserId();
        List<EvaluationForm> forms = formMapper.selectList(new LambdaQueryWrapper<EvaluationForm>()
                .eq(EvaluationForm::getTenantId, tenantId)
                .eq(EvaluationForm::getStatus, "published")
                .orderByDesc(EvaluationForm::getPublishedAt)
                .orderByDesc(EvaluationForm::getCreatedAt));
        List<StudentEvaluationVO.Task> tasks = new ArrayList<>();
        for (EvaluationForm form : forms) {
            if (!canStudentAccessForm(tenantId, userId, form)) {
                continue;
            }
            EvaluationWindow window = findWindow(tenantId, form.getId());
            if (window == null) {
                continue;
            }
            EvaluationSubmission submission = findSubmission(tenantId, form.getId(), userId);
            StatusInfo status = taskStatus(window, submission);
            TargetInfo target = resolveTarget(form);
            tasks.add(StudentEvaluationVO.Task.builder()
                    .id(window.getId())
                    .title(form.getTitle())
                    .type(typeLabel(normalizeFormType(form.getType())))
                    .typeKey(normalizeFormType(form.getType()))
                    .dept(target.deptName())
                    .target(target.targetName())
                    .startDate(formatShort(window.getStartAt()))
                    .endDate(formatShort(window.getEndAt()))
                    .status(status.label())
                    .statusType(status.type())
                    .img(form.getCoverFileId() != null ? helperMapper.selectFileUrlById(form.getCoverFileId()) : null)
                    .formId(form.getId())
                    .build());
        }
        return tasks;
    }

    @Override
    public StudentEvaluationVO.SubmitPage getSubmitPage(Long formId) {
        Long tenantId = requireTenantId();
        Long userId = requireUserId();
        EvaluationForm form = getPublishedForm(tenantId, formId);
        if (!canStudentAccessForm(tenantId, userId, form)) {
            throw new BusinessException(403, "Current user cannot evaluate this form");
        }
        EvaluationWindow window = findWindow(tenantId, formId);
        EvaluationSubmission submission = findSubmission(tenantId, formId, userId);
        PageMode pageMode = pageMode(window, submission);
        List<EvaluationQuestion> questions = listQuestions(tenantId, formId);
        Map<Long, List<StudentEvaluationVO.Option>> options = buildOptions(tenantId, questions);

        List<StudentEvaluationVO.Answer> answers = List.of();
        List<StudentEvaluationVO.Score> scores = List.of();
        List<StudentEvaluationVO.Attachment> attachments = List.of();
        if (submission != null && List.of("draft", "edit", "readonly").contains(pageMode.mode())) {
            answers = listAnswers(tenantId, submission.getId());
            scores = listScores(tenantId, submission.getId());
            attachments = listAttachments(tenantId, submission.getId());
        }

        return StudentEvaluationVO.SubmitPage.builder()
                .form(toFormVO(form))
                .window(window != null ? toWindowVO(window) : null)
                .questions(questions.stream().map(this::toQuestionVO).toList())
                .options(options)
                .submission(submission != null ? toSubmissionVO(submission) : null)
                .answers(answers)
                .scores(scores)
                .attachments(attachments)
                .mode(pageMode.mode())
                .readonly(pageMode.readonly())
                .readonlyReason(pageMode.reason())
                .build();
    }

    @Override
    @Transactional
    public StudentEvaluationVO.Submission saveDraft(StudentEvaluationSubmitDTO dto) {
        Long tenantId = requireTenantId();
        Long userId = requireUserId();
        Long schoolId = SecurityUtils.getSchoolId();
        EvaluationForm form = getPublishedForm(tenantId, dto.getFormId());
        EvaluationWindow window = requireWindow(tenantId, form.getId());
        assertInsideWindow(window, true);
        assertFormAccess(tenantId, userId, form);

        EvaluationSubmission submission = dto.getSubmissionId() != null
                ? getOwnedSubmission(tenantId, userId, dto.getSubmissionId())
                : findSubmission(tenantId, form.getId(), userId);
        if (submission != null && !"draft".equals(submission.getStatus())) {
            throw new BusinessException(409, "Submitted evaluation cannot be saved as draft");
        }
        if (submission == null) {
            submission = newSubmission(tenantId, schoolId, userId, form, window);
            submission.setStatus("draft");
            submissionMapper.insert(submission);
        }
        replaceDetailRows(tenantId, schoolId, submission.getId(), dto.getAnswers(), dto.getScores(), dto.getAttachments());
        submission.setOverallScore(calculateOverallScore(dto.getScores()));
        submissionMapper.updateById(submission);
        return toSubmissionVO(submission);
    }

    @Override
    @Transactional
    public StudentEvaluationVO.Submission submit(StudentEvaluationSubmitDTO dto) {
        Long tenantId = requireTenantId();
        Long userId = requireUserId();
        Long schoolId = SecurityUtils.getSchoolId();
        EvaluationForm form = getPublishedForm(tenantId, dto.getFormId());
        EvaluationWindow window = requireWindow(tenantId, form.getId());
        assertInsideWindow(window, false);
        assertFormAccess(tenantId, userId, form);

        List<EvaluationQuestion> questions = listQuestions(tenantId, form.getId());
        validateRequiredAnswers(questions, dto.getAnswers(), dto.getScores());

        EvaluationSubmission submission = dto.getSubmissionId() != null
                ? getOwnedSubmission(tenantId, userId, dto.getSubmissionId())
                : findSubmission(tenantId, form.getId(), userId);
        if (submission != null && "submitted".equals(submission.getStatus())) {
            assertModifiable(submission);
        }
        if (submission == null) {
            submission = newSubmission(tenantId, schoolId, userId, form, window);
            submissionMapper.insert(submission);
        }
        LocalDateTime now = LocalDateTime.now();
        submission.setStatus("submitted");
        if (submission.getSubmittedAt() == null) {
            submission.setSubmittedAt(now);
        }
        int hours = dto.getModifiableHours() != null ? dto.getModifiableHours()
                : window.getModifiableHours() != null ? window.getModifiableHours() : 24;
        if (submission.getModifiableUntil() == null) {
            submission.setModifiableUntil(now.plusHours(hours));
        }
        submission.setReviewStatus(dto.getReviewStatus());
        submission.setOverallScore(calculateOverallScore(dto.getScores()));
        replaceDetailRows(tenantId, schoolId, submission.getId(), dto.getAnswers(), dto.getScores(), dto.getAttachments());
        submissionMapper.updateById(submission);
        return toSubmissionVO(submission);
    }

    @Override
    @Transactional
    public StudentEvaluationVO.Submission updateSubmitted(Long submissionId, StudentEvaluationUpdateDTO dto) {
        Long tenantId = requireTenantId();
        Long userId = requireUserId();
        EvaluationSubmission submission = getOwnedSubmission(tenantId, userId, submissionId);
        if (!"submitted".equals(submission.getStatus())) {
            throw new BusinessException(409, "Only submitted evaluations can be updated here");
        }
        assertModifiable(submission);
        List<EvaluationQuestion> questions = listQuestions(tenantId, submission.getFormId());
        validateRequiredAnswers(questions, dto.getAnswers(), dto.getScores());
        submission.setReviewStatus(dto.getReviewStatus() != null ? dto.getReviewStatus() : submission.getReviewStatus());
        submission.setOverallScore(calculateOverallScore(dto.getScores()));
        replaceDetailRows(tenantId, submission.getSchoolId(), submission.getId(), dto.getAnswers(), dto.getScores(), dto.getAttachments());
        submissionMapper.updateById(submission);
        return toSubmissionVO(submission);
    }

    @Override
    public List<StudentEvaluationVO.History> listMySubmissions() {
        Long tenantId = requireTenantId();
        Long userId = requireUserId();
        List<EvaluationSubmission> submissions = submissionMapper.selectList(new LambdaQueryWrapper<EvaluationSubmission>()
                .eq(EvaluationSubmission::getTenantId, tenantId)
                .eq(EvaluationSubmission::getEvaluatorUserId, userId)
                .orderByDesc(EvaluationSubmission::getUpdatedAt));
        Map<Long, StudentEvaluationVO.History> latestByForm = new LinkedHashMap<>();
        for (EvaluationSubmission submission : submissions) {
            latestByForm.putIfAbsent(submission.getFormId(), toHistory(submission));
        }
        return new ArrayList<>(latestByForm.values());
    }

    @Override
    public List<StudentEvaluationVO.SimpleOption> listCourses() {
        Long tenantId = requireTenantId();
        return helperMapper.selectCourseOptions(tenantId).stream().map(row -> StudentEvaluationVO.SimpleOption.builder()
                .id(asLong(row.get("id")))
                .value(asLong(row.get("id")))
                .label(asString(row.get("course_name")))
                .courseName(asString(row.get("course_name")))
                .teachingOrgId(asLong(row.get("teaching_org_id")))
                .build()).toList();
    }

    @Override
    public List<StudentEvaluationVO.SimpleOption> listCourseEnrollments() {
        Long tenantId = requireTenantId();
        Long userId = requireUserId();
        return helperMapper.selectStudentCourseEnrollments(tenantId, userId).stream()
                .map(row -> StudentEvaluationVO.SimpleOption.builder()
                        .courseId(asLong(row.get("course_id")))
                        .build()).toList();
    }

    @Override
    public List<StudentEvaluationVO.SimpleOption> listServiceItems() {
        Long tenantId = requireTenantId();
        return helperMapper.selectServiceItemOptions(tenantId).stream().map(row -> StudentEvaluationVO.SimpleOption.builder()
                .id(asLong(row.get("id")))
                .value(asLong(row.get("id")))
                .label(asString(row.get("name")))
                .name(asString(row.get("name")))
                .serviceOrgId(asLong(row.get("service_org_id")))
                .build()).toList();
    }

    @Override
    public List<StudentEvaluationVO.SimpleOption> listTeachingOrgs() {
        Long tenantId = requireTenantId();
        return helperMapper.selectTeachingOrgOptions(tenantId).stream().map(row -> StudentEvaluationVO.SimpleOption.builder()
                .id(asLong(row.get("id")))
                .value(asLong(row.get("id")))
                .label(asString(row.get("name")))
                .name(asString(row.get("name")))
                .build()).toList();
    }

    private EvaluationSubmission newSubmission(Long tenantId, Long schoolId, Long userId,
                                               EvaluationForm form, EvaluationWindow window) {
        TargetInfo target = resolveTarget(form);
        EvaluationSubmission submission = new EvaluationSubmission();
        submission.setTenantId(tenantId);
        submission.setSchoolId(schoolId != null ? schoolId : form.getSchoolId());
        submission.setFormId(form.getId());
        submission.setWindowId(window.getId());
        submission.setEvaluatorUserId(userId);
        submission.setTargetType(target.targetType());
        submission.setTargetId(target.targetId());
        submission.setAnonymous(Boolean.TRUE.equals(form.getAnonymous()));
        return submission;
    }

    private void replaceDetailRows(Long tenantId, Long schoolId, Long submissionId,
                                   List<StudentEvaluationSubmitDTO.AnswerItem> answers,
                                   List<StudentEvaluationSubmitDTO.ScoreItem> scores,
                                   List<StudentEvaluationSubmitDTO.AttachmentItem> attachments) {
        answerMapper.delete(new LambdaQueryWrapper<EvaluationAnswer>()
                .eq(EvaluationAnswer::getTenantId, tenantId)
                .eq(EvaluationAnswer::getSubmissionId, submissionId));
        scoreMapper.delete(new LambdaQueryWrapper<EvaluationScore>()
                .eq(EvaluationScore::getTenantId, tenantId)
                .eq(EvaluationScore::getSubmissionId, submissionId));
        attachmentMapper.delete(new LambdaQueryWrapper<EvaluationAttachment>()
                .eq(EvaluationAttachment::getTenantId, tenantId)
                .eq(EvaluationAttachment::getSubmissionId, submissionId));

        if (answers != null) {
            for (StudentEvaluationSubmitDTO.AnswerItem item : answers) {
                if (item.getQuestionId() == null || item.getAnswerValue() == null) {
                    continue;
                }
                EvaluationAnswer answer = new EvaluationAnswer();
                answer.setTenantId(tenantId);
                answer.setSchoolId(schoolId);
                answer.setSubmissionId(submissionId);
                answer.setQuestionId(item.getQuestionId());
                answer.setAnswerValue(serializeAnswerValue(item.getAnswerValue()));
                answerMapper.insert(answer);
            }
        }
        if (scores != null) {
            for (StudentEvaluationSubmitDTO.ScoreItem item : scores) {
                if (item.getQuestionId() == null || item.getScore() == null) {
                    continue;
                }
                EvaluationScore score = new EvaluationScore();
                score.setTenantId(tenantId);
                score.setSchoolId(schoolId);
                score.setSubmissionId(submissionId);
                score.setQuestionId(item.getQuestionId());
                score.setScore(item.getScore());
                scoreMapper.insert(score);
            }
        }
        if (attachments != null) {
            for (StudentEvaluationSubmitDTO.AttachmentItem item : attachments) {
                if (item.getFileId() == null) {
                    continue;
                }
                EvaluationAttachment attachment = new EvaluationAttachment();
                attachment.setTenantId(tenantId);
                attachment.setSchoolId(schoolId);
                attachment.setSubmissionId(submissionId);
                attachment.setQuestionId(item.getQuestionId());
                attachment.setFileId(item.getFileId());
                attachmentMapper.insert(attachment);
            }
        }
    }

    private void validateRequiredAnswers(List<EvaluationQuestion> questions,
                                         List<StudentEvaluationSubmitDTO.AnswerItem> answers,
                                         List<StudentEvaluationSubmitDTO.ScoreItem> scores) {
        Map<Long, Object> answerMap = answers == null ? Map.of() : answers.stream()
                .filter(a -> a.getQuestionId() != null)
                .collect(Collectors.toMap(StudentEvaluationSubmitDTO.AnswerItem::getQuestionId,
                        StudentEvaluationSubmitDTO.AnswerItem::getAnswerValue, (a, b) -> b));
        Map<Long, BigDecimal> scoreMap = scores == null ? Map.of() : scores.stream()
                .filter(s -> s.getQuestionId() != null)
                .collect(Collectors.toMap(StudentEvaluationSubmitDTO.ScoreItem::getQuestionId,
                        StudentEvaluationSubmitDTO.ScoreItem::getScore, (a, b) -> b));
        for (EvaluationQuestion question : questions) {
            if (!Boolean.TRUE.equals(question.getRequired())) {
                continue;
            }
            if ("rating".equals(question.getType())) {
                if (scoreMap.get(question.getId()) == null) {
                    throw new BusinessException(400, "Required rating question is missing");
                }
                continue;
            }
            Object value = answerMap.get(question.getId());
            if (isBlankAnswer(value)) {
                throw new BusinessException(400, "Required question is missing");
            }
            if ("text".equals(question.getType()) && question.getMinLength() != null
                    && String.valueOf(value).trim().length() < question.getMinLength()) {
                throw new BusinessException(400, "Text answer is shorter than required");
            }
        }
    }

    private boolean isBlankAnswer(Object value) {
        if (value == null) {
            return true;
        }
        if (value instanceof String str) {
            return str.trim().isEmpty();
        }
        if (value instanceof List<?> list) {
            return list.isEmpty();
        }
        return false;
    }

    private BigDecimal calculateOverallScore(List<StudentEvaluationSubmitDTO.ScoreItem> scores) {
        if (scores == null || scores.isEmpty()) {
            return null;
        }
        List<BigDecimal> values = scores.stream()
                .map(StudentEvaluationSubmitDTO.ScoreItem::getScore)
                .filter(Objects::nonNull)
                .toList();
        if (values.isEmpty()) {
            return null;
        }
        BigDecimal total = values.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return total.divide(BigDecimal.valueOf(values.size()), 2, RoundingMode.HALF_UP);
    }

    private void assertFormAccess(Long tenantId, Long userId, EvaluationForm form) {
        if (!canStudentAccessForm(tenantId, userId, form)) {
            throw new BusinessException(403, "Current user cannot evaluate this form");
        }
    }

    private boolean canStudentAccessForm(Long tenantId, Long userId, EvaluationForm form) {
        if (form.getCourseId() != null) {
            return helperMapper.countActiveCourseEnrollment(tenantId, form.getCourseId(), userId) > 0;
        }
        return form.getServiceItemId() != null;
    }

    private void assertInsideWindow(EvaluationWindow window, boolean allowNotStartedDraft) {
        LocalDateTime now = LocalDateTime.now();
        if (!allowNotStartedDraft && window.getStartAt() != null && now.isBefore(window.getStartAt())) {
            throw new BusinessException(409, "Evaluation window has not started");
        }
        if (window.getEndAt() != null && now.isAfter(window.getEndAt())) {
            throw new BusinessException(409, "Evaluation window has ended");
        }
    }

    private void assertModifiable(EvaluationSubmission submission) {
        if (submission.getLockedAt() != null) {
            throw new BusinessException(409, "Evaluation has been locked");
        }
        if (submission.getModifiableUntil() == null || LocalDateTime.now().isAfter(submission.getModifiableUntil())) {
            throw new BusinessException(409, "Modification window has expired");
        }
    }

    private EvaluationForm getPublishedForm(Long tenantId, Long formId) {
        EvaluationForm form = formMapper.selectOne(new LambdaQueryWrapper<EvaluationForm>()
                .eq(EvaluationForm::getTenantId, tenantId)
                .eq(EvaluationForm::getId, formId));
        if (form == null) {
            throw new BusinessException(404, "Evaluation form not found");
        }
        if (!"published".equals(form.getStatus())) {
            throw new BusinessException(409, "Evaluation form is not published");
        }
        return form;
    }

    private EvaluationWindow requireWindow(Long tenantId, Long formId) {
        EvaluationWindow window = findWindow(tenantId, formId);
        if (window == null) {
            throw new BusinessException(404, "Evaluation window not found");
        }
        return window;
    }

    private EvaluationWindow findWindow(Long tenantId, Long formId) {
        return windowMapper.selectOne(new LambdaQueryWrapper<EvaluationWindow>()
                .eq(EvaluationWindow::getTenantId, tenantId)
                .eq(EvaluationWindow::getFormId, formId));
    }

    private EvaluationSubmission findSubmission(Long tenantId, Long formId, Long userId) {
        return submissionMapper.selectOne(new LambdaQueryWrapper<EvaluationSubmission>()
                .eq(EvaluationSubmission::getTenantId, tenantId)
                .eq(EvaluationSubmission::getFormId, formId)
                .eq(EvaluationSubmission::getEvaluatorUserId, userId)
                .orderByDesc(EvaluationSubmission::getUpdatedAt)
                .last("LIMIT 1"));
    }

    private EvaluationSubmission getOwnedSubmission(Long tenantId, Long userId, Long id) {
        EvaluationSubmission submission = submissionMapper.selectOne(new LambdaQueryWrapper<EvaluationSubmission>()
                .eq(EvaluationSubmission::getTenantId, tenantId)
                .eq(EvaluationSubmission::getEvaluatorUserId, userId)
                .eq(EvaluationSubmission::getId, id));
        if (submission == null) {
            throw new BusinessException(404, "Evaluation submission not found");
        }
        return submission;
    }

    private List<EvaluationQuestion> listQuestions(Long tenantId, Long formId) {
        return questionMapper.selectList(new LambdaQueryWrapper<EvaluationQuestion>()
                .eq(EvaluationQuestion::getTenantId, tenantId)
                .eq(EvaluationQuestion::getFormId, formId)
                .orderByAsc(EvaluationQuestion::getSortOrder));
    }

    private Map<Long, List<StudentEvaluationVO.Option>> buildOptions(Long tenantId, List<EvaluationQuestion> questions) {
        Map<Long, List<StudentEvaluationVO.Option>> result = new HashMap<>();
        for (EvaluationQuestion question : questions) {
            if (!List.of("single", "multiple").contains(question.getType())) {
                continue;
            }
            List<EvaluationQuestionOption> options = optionMapper.selectList(new LambdaQueryWrapper<EvaluationQuestionOption>()
                    .eq(EvaluationQuestionOption::getTenantId, tenantId)
                    .eq(EvaluationQuestionOption::getQuestionId, question.getId())
                    .orderByAsc(EvaluationQuestionOption::getSortOrder));
            result.put(question.getId(), options.stream().map(o -> StudentEvaluationVO.Option.builder()
                    .id(o.getId())
                    .label(o.getOptionText())
                    .optionText(o.getOptionText())
                    .sortOrder(o.getSortOrder())
                    .build()).toList());
        }
        return result;
    }

    private List<StudentEvaluationVO.Answer> listAnswers(Long tenantId, Long submissionId) {
        return answerMapper.selectList(new LambdaQueryWrapper<EvaluationAnswer>()
                        .eq(EvaluationAnswer::getTenantId, tenantId)
                        .eq(EvaluationAnswer::getSubmissionId, submissionId))
                .stream().map(a -> StudentEvaluationVO.Answer.builder()
                        .id(a.getId())
                        .questionId(a.getQuestionId())
                        .answerValue(parseAnswerValue(a.getAnswerValue()))
                        .build()).toList();
    }

    private List<StudentEvaluationVO.Score> listScores(Long tenantId, Long submissionId) {
        return scoreMapper.selectList(new LambdaQueryWrapper<EvaluationScore>()
                        .eq(EvaluationScore::getTenantId, tenantId)
                        .eq(EvaluationScore::getSubmissionId, submissionId))
                .stream().map(s -> StudentEvaluationVO.Score.builder()
                        .id(s.getId())
                        .questionId(s.getQuestionId())
                        .score(s.getScore())
                        .build()).toList();
    }

    private List<StudentEvaluationVO.Attachment> listAttachments(Long tenantId, Long submissionId) {
        return attachmentMapper.selectList(new LambdaQueryWrapper<EvaluationAttachment>()
                        .eq(EvaluationAttachment::getTenantId, tenantId)
                        .eq(EvaluationAttachment::getSubmissionId, submissionId))
                .stream().map(a -> StudentEvaluationVO.Attachment.builder()
                        .id(a.getId())
                        .questionId(a.getQuestionId())
                        .fileId(a.getFileId())
                        .build()).toList();
    }

    private StudentEvaluationVO.Form toFormVO(EvaluationForm form) {
        TargetInfo target = resolveTarget(form);
        String type = normalizeFormType(form.getType());
        return StudentEvaluationVO.Form.builder()
                .id(form.getId())
                .title(form.getTitle())
                .description(form.getDescription())
                .type(type)
                .status(form.getStatus())
                .publishScope(form.getPublishScope())
                .courseId(form.getCourseId())
                .serviceItemId(form.getServiceItemId())
                .coverFileId(form.getCoverFileId())
                .anonymous(form.getAnonymous())
                .scoreEnabled(form.getScoreEnabled())
                .targetName(target.targetName())
                .deptName(target.deptName())
                .typeLabel(typeLabel(type))
                .build();
    }

    private StudentEvaluationVO.Window toWindowVO(EvaluationWindow window) {
        return StudentEvaluationVO.Window.builder()
                .id(window.getId())
                .formId(window.getFormId())
                .type(normalizeWindowType(window))
                .startAt(window.getStartAt())
                .endAt(window.getEndAt())
                .modifiableHours(window.getModifiableHours())
                .status(window.getStatus())
                .build();
    }

    private StudentEvaluationVO.Question toQuestionVO(EvaluationQuestion question) {
        return StudentEvaluationVO.Question.builder()
                .id(question.getId())
                .type(question.getType())
                .title(question.getTitle())
                .required(question.getRequired())
                .maxScore(question.getMaxScore())
                .minLength(question.getMinLength())
                .sortOrder(question.getSortOrder())
                .build();
    }

    private StudentEvaluationVO.Submission toSubmissionVO(EvaluationSubmission submission) {
        return StudentEvaluationVO.Submission.builder()
                .id(submission.getId())
                .formId(submission.getFormId())
                .windowId(submission.getWindowId())
                .targetType(submission.getTargetType())
                .targetId(submission.getTargetId())
                .evaluatorUserId(submission.getEvaluatorUserId())
                .submittedAt(submission.getSubmittedAt())
                .modifiableUntil(submission.getModifiableUntil())
                .lockedAt(submission.getLockedAt())
                .status(submission.getStatus())
                .reviewStatus(submission.getReviewStatus())
                .createdAt(submission.getCreatedAt())
                .updatedAt(submission.getUpdatedAt())
                .build();
    }

    private StudentEvaluationVO.History toHistory(EvaluationSubmission submission) {
        EvaluationForm form = formMapper.selectById(submission.getFormId());
        TargetInfo target = form != null ? resolveTarget(form) : TargetInfo.empty();
        String displayStatus = displayStatus(submission);
        String formType = form != null ? normalizeFormType(form.getType()) : null;
        return StudentEvaluationVO.History.builder()
                .id(submission.getId())
                .formId(submission.getFormId())
                .formTitle(form != null ? form.getTitle() : "Evaluation #" + submission.getFormId())
                .formType(formType)
                .displayStatus(displayStatus)
                .canEdit("draft".equals(displayStatus) || "modifiable".equals(displayStatus))
                .canView(true)
                .coverImg(form != null && form.getCoverFileId() != null ? helperMapper.selectFileUrlById(form.getCoverFileId()) : null)
                .targetName(target.targetName())
                .deptName(target.deptName())
                .submittedAt(submission.getSubmittedAt())
                .modifiableUntil(submission.getModifiableUntil())
                .updatedAt(submission.getUpdatedAt())
                .status(submission.getStatus())
                .build();
    }

    private String displayStatus(EvaluationSubmission submission) {
        if ("draft".equals(submission.getStatus())) {
            return "draft";
        }
        if ("pending".equals(submission.getReviewStatus())) {
            return "pending";
        }
        if (submission.getLockedAt() != null) {
            return "locked";
        }
        if ("submitted".equals(submission.getStatus())
                && submission.getModifiableUntil() != null
                && !LocalDateTime.now().isAfter(submission.getModifiableUntil())) {
            return "modifiable";
        }
        return "locked";
    }

    private PageMode pageMode(EvaluationWindow window, EvaluationSubmission submission) {
        if (window == null) {
            return new PageMode("closed", true, "Evaluation window is not configured");
        }
        LocalDateTime now = LocalDateTime.now();
        if (submission != null) {
            if ("draft".equals(submission.getStatus())) {
                if (window.getEndAt() != null && now.isAfter(window.getEndAt())) {
                    return new PageMode("closed", true, "Evaluation window has ended");
                }
                return new PageMode("draft", false, "");
            }
            if ("submitted".equals(submission.getStatus())) {
                if (submission.getLockedAt() != null) {
                    return new PageMode("readonly", true, "Evaluation has been locked");
                }
                if (submission.getModifiableUntil() != null && !now.isAfter(submission.getModifiableUntil())) {
                    return new PageMode("edit", false, "");
                }
                return new PageMode("readonly", true, "Modification window has expired");
            }
            return new PageMode("readonly", true, "Evaluation status is not editable");
        }
        if (window.getStartAt() != null && now.isBefore(window.getStartAt())) {
            return new PageMode("not_started", true, "Evaluation window has not started");
        }
        if (window.getEndAt() != null && now.isAfter(window.getEndAt())) {
            return new PageMode("closed", true, "Evaluation window has ended");
        }
        return new PageMode("create", false, "");
    }

    private StatusInfo taskStatus(EvaluationWindow window, EvaluationSubmission submission) {
        LocalDateTime now = LocalDateTime.now();
        if (window.getStartAt() != null && now.isBefore(window.getStartAt())) {
            return new StatusInfo("未开始", "info");
        }
        if (window.getEndAt() != null && now.isAfter(window.getEndAt())) {
            return submission != null && "submitted".equals(submission.getStatus())
                    ? new StatusInfo("已完成", "success") : new StatusInfo("已截止", "info");
        }
        if (submission == null) {
            return new StatusInfo("待评价", "warning");
        }
        if ("draft".equals(submission.getStatus())) {
            return new StatusInfo("进行中", "primary");
        }
        if ("submitted".equals(submission.getStatus())) {
            if (submission.getLockedAt() != null) {
                return new StatusInfo("已完成", "success");
            }
            if (submission.getModifiableUntil() != null && !now.isAfter(submission.getModifiableUntil())) {
                return new StatusInfo("可修改", "warning");
            }
            return new StatusInfo("已完成", "success");
        }
        return new StatusInfo("进行中", "primary");
    }

    private TargetInfo resolveTarget(EvaluationForm form) {
        if (form.getCourseId() != null) {
            Map<String, Object> row = helperMapper.selectCourseInfoById(form.getCourseId());
            return new TargetInfo("course", form.getCourseId(),
                    row != null ? asString(row.get("course_name")) : "",
                    row != null ? asString(row.get("org_name")) : "教务处");
        }
        if (form.getServiceItemId() != null) {
            Map<String, Object> row = helperMapper.selectServiceItemInfoById(form.getServiceItemId());
            return new TargetInfo("service_item", form.getServiceItemId(),
                    row != null ? asString(row.get("name")) : "",
                    row != null ? asString(row.get("org_name")) : "后勤管理处");
        }
        return TargetInfo.empty();
    }

    private String serializeAnswerValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof String str) {
            return str;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            return String.valueOf(value);
        }
    }

    private Object parseAnswerValue(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.startsWith("[") || trimmed.startsWith("{")) {
            try {
                return objectMapper.readValue(trimmed, Object.class);
            } catch (JsonProcessingException ignored) {
                return value;
            }
        }
        return value;
    }

    private String normalizeFormType(String type) {
        if ("course_evaluation".equals(type) || "teaching".equals(type)) {
            return "teaching";
        }
        if ("service_evaluation".equals(type) || "service".equals(type)) {
            return "service";
        }
        return type != null ? type : "service";
    }

    private String normalizeWindowType(EvaluationWindow window) {
        EvaluationForm form = formMapper.selectById(window.getFormId());
        return form != null ? normalizeFormType(form.getType()) : window.getType();
    }

    private String typeLabel(String type) {
        return switch (type) {
            case "teaching" -> "教学评价";
            case "instant" -> "即时评价";
            default -> "后勤服务";
        };
    }

    private String formatShort(LocalDateTime time) {
        return time != null ? SHORT_DATE.format(time) : "";
    }

    private Long requireTenantId() {
        Long tenantId = SecurityUtils.getTenantId();
        if (tenantId == null) {
            throw new BusinessException(403, "Tenant context is missing");
        }
        return tenantId;
    }

    private Long requireUserId() {
        Long userId = SecurityUtils.getUserId();
        if (userId == null) {
            throw new BusinessException(401, "Login required");
        }
        if (!SecurityUtils.hasRole("student")) {
            throw new BusinessException(403, "Student role required");
        }
        return userId;
    }

    private Long asLong(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value != null) {
            return Long.valueOf(String.valueOf(value));
        }
        return null;
    }

    private String asString(Object value) {
        return value != null ? String.valueOf(value) : "";
    }

    private record TargetInfo(String targetType, Long targetId, String targetName, String deptName) {
        static TargetInfo empty() {
            return new TargetInfo(null, null, "", "");
        }
    }

    private record PageMode(String mode, boolean readonly, String reason) {
    }

    private record StatusInfo(String label, String type) {
    }
}
