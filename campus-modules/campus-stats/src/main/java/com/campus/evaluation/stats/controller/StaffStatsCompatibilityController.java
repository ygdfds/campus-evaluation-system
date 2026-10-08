package com.campus.evaluation.stats.controller;

import com.campus.evaluation.common.core.domain.R;
import com.campus.evaluation.common.core.exception.BusinessException;
import com.campus.evaluation.common.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * Compatibility read endpoints for the legacy staff pages.
 *
 * The frontend originally read these resources from json-server. Until every
 * page is switched to the domain APIs, these endpoints expose the same read
 * shape from the real tenant database.
 */
@RestController
@RequiredArgsConstructor
public class StaffStatsCompatibilityController {

    private final JdbcTemplate jdbcTemplate;

    @GetMapping("/evaluationForms")
    public R<List<Map<String, Object>>> forms(@RequestParam Map<String, String> params) {
        return R.ok(query("""
                SELECT id, tenant_id, school_id, type, title, description, cover_file_id,
                       publisher_id, publish_scope, anonymous, score_enabled, teaching_org_id,
                       service_org_id, course_id, service_item_id, status, published_at,
                       created_at, updated_at, deleted
                FROM eval_form
                """, params, "status"));
    }

    @GetMapping("/evaluationWindows")
    public R<List<Map<String, Object>>> windows(@RequestParam Map<String, String> params) {
        return R.ok(query("""
                SELECT id, tenant_id, school_id, form_id, type, start_at, end_at,
                       modifiable_hours, status, created_at, updated_at, deleted
                FROM eval_window
                """, params, "status"));
    }

    @GetMapping("/evaluationSubmissions")
    public R<List<Map<String, Object>>> submissions(@RequestParam Map<String, String> params) {
        return R.ok(query("""
                SELECT id, tenant_id, school_id, form_id, window_id, evaluator_user_id,
                       target_type, target_id, overall_score, anonymous, submitted_at,
                       modifiable_until, locked_at, status, review_status, created_at,
                       updated_at, deleted
                FROM eval_submission
                """, params, "status"));
    }

    @GetMapping("/evaluationScores")
    public R<List<Map<String, Object>>> scores(@RequestParam Map<String, String> params) {
        return R.ok(query("""
                SELECT id, tenant_id, school_id, submission_id, question_id, score,
                       created_at, updated_at, deleted
                FROM eval_score
                """, params, null));
    }

    @GetMapping("/evaluationAnswers")
    public R<List<Map<String, Object>>> answers(@RequestParam Map<String, String> params) {
        return R.ok(query("""
                SELECT id, tenant_id, school_id, submission_id, question_id, answer_value,
                       created_at, updated_at, deleted
                FROM eval_answer
                """, params, null));
    }

    @GetMapping("/evaluationQuestions")
    public R<List<Map<String, Object>>> questions(@RequestParam Map<String, String> params) {
        return R.ok(query("""
                SELECT id, tenant_id, school_id, form_id, type, title, required, max_score,
                       min_length, sort_order, created_at, updated_at, deleted
                FROM eval_question
                """, params, null));
    }

    @GetMapping("/evaluationQuestionOptions")
    public R<List<Map<String, Object>>> questionOptions(@RequestParam Map<String, String> params) {
        return R.ok(query("""
                SELECT id, tenant_id, school_id, question_id, option_text, sort_order,
                       created_at, updated_at, deleted
                FROM eval_question_option
                """, params, null));
    }

    @GetMapping("/courses")
    public R<List<Map<String, Object>>> courses(@RequestParam Map<String, String> params) {
        return R.ok(query("""
                SELECT id, tenant_id, school_id, teaching_org_id, course_code, course_name,
                       term, start_at, end_at, created_at, updated_at, deleted
                FROM sch_course
                """, params, null));
    }

    @GetMapping("/courseTeachers")
    public R<List<Map<String, Object>>> courseTeachers(@RequestParam Map<String, String> params) {
        return R.ok(query("""
                SELECT id, tenant_id, school_id, course_id, teacher_id, role_status,
                       created_at, updated_at, deleted
                FROM sch_course_teacher
                """, params, null));
    }

    @GetMapping("/teachingOrgUnits")
    public R<List<Map<String, Object>>> teachingOrgUnits(@RequestParam Map<String, String> params) {
        return R.ok(query("""
                SELECT id, tenant_id, school_id, parent_id, name, code, type, status,
                       created_at, updated_at, deleted
                FROM sch_teaching_org_unit
                """, params, null));
    }

    @GetMapping("/serviceItems")
    public R<List<Map<String, Object>>> serviceItems(@RequestParam Map<String, String> params) {
        return R.ok(query("""
                SELECT id, tenant_id, school_id, service_org_id, name, cover_file_id, type,
                       status, created_at, updated_at, deleted
                FROM sch_service_item
                """, params, null));
    }

    @GetMapping("/serviceOrgUnits")
    public R<List<Map<String, Object>>> serviceOrgUnits(@RequestParam Map<String, String> params) {
        return R.ok(query("""
                SELECT id, tenant_id, school_id, parent_id, name, code, type, status,
                       created_at, updated_at, deleted
                FROM sch_service_org_unit
                """, params, null));
    }

    @GetMapping("/formPublishAudits")
    public R<List<Map<String, Object>>> formPublishAudits(@RequestParam Map<String, String> params) {
        return R.ok(query("""
                SELECT id, tenant_id, school_id, form_id, action, status, requested_by,
                       requested_at, submitter_role, submit_reason, reviewed_by, reviewed_at,
                       review_comment, created_at, updated_at, deleted
                FROM eval_form_publish_audit
                """, params, "status"));
    }

    @GetMapping("/fileResources")
    public R<List<Map<String, Object>>> fileResources(@RequestParam Map<String, String> params) {
        return R.ok(query("""
                SELECT id, tenant_id, school_id, object_key, file_name, mime_type, url,
                       uploader_id, size, biz_type, biz_id, created_at, updated_at, deleted
                FROM file_resource
                """, params, "biz_type"));
    }

    @GetMapping("/appealRequests")
    public R<List<Map<String, Object>>> appealRequests() {
        return R.ok(List.of());
    }

    private List<Map<String, Object>> query(String baseSql,
                                            Map<String, String> params,
                                            String optionalFilter) {
        requireStaffAccess();
        Long tenantId = SecurityUtils.getTenantId();
        if (tenantId == null) {
            throw new BusinessException(403, "无法获取租户信息");
        }

        StringBuilder sql = new StringBuilder(baseSql);
        sql.append(" WHERE tenant_id = ? AND deleted = 0");
        Object[] args = new Object[]{tenantId};

        if (optionalFilter != null && params.get(optionalFilter) != null
                && !params.get(optionalFilter).isBlank()) {
            sql.append(" AND ").append(optionalFilter).append(" = ?");
            args = new Object[]{tenantId, params.get(optionalFilter)};
        }

        if (params.get("id") != null && !params.get("id").isBlank()) {
            sql.append(" AND id = ?");
            Object[] nextArgs = new Object[args.length + 1];
            System.arraycopy(args, 0, nextArgs, 0, args.length);
            nextArgs[args.length] = params.get("id");
            args = nextArgs;
        }

        sql.append(" ORDER BY id DESC");
        try {
            return jdbcTemplate.queryForList(sql.toString(), args);
        } catch (DataAccessException ex) {
            throw new BusinessException(500, "统计数据查询失败");
        }
    }

    private void requireStaffAccess() {
        if (SecurityUtils.hasRole("student")) {
            throw new BusinessException(403, "无权访问员工统计数据");
        }
    }
}
