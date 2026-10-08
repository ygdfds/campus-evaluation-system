package com.campus.evaluation.stats.service.impl;

import com.campus.evaluation.common.core.exception.BusinessException;
import com.campus.evaluation.common.security.SecurityUtils;
import com.campus.evaluation.stats.service.StaffStatsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class StaffStatsServiceImpl implements StaffStatsService {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Map<String, Object> reportContext() {
        Long tenantId = requireTenantId();
        Map<String, Object> data = new HashMap<>();
        data.put("forms", queryList("""
                SELECT id, tenant_id, school_id, type, title, description, cover_file_id, publisher_id,
                       publish_scope, anonymous, score_enabled, teaching_org_id, service_org_id,
                       course_id, service_item_id, status, published_at, created_at, updated_at, deleted
                FROM eval_form WHERE tenant_id = ? AND deleted = 0
                """, tenantId));
        data.put("windows", queryList("""
                SELECT id, tenant_id, school_id, form_id, type, start_at, end_at,
                       modifiable_hours, status, created_at, updated_at, deleted
                FROM eval_window WHERE tenant_id = ? AND deleted = 0
                """, tenantId));
        data.put("submissions", queryList("""
                SELECT id, tenant_id, school_id, form_id, window_id, evaluator_user_id,
                       target_type, target_id, overall_score, anonymous, submitted_at,
                       modifiable_until, locked_at, status, review_status, created_at, updated_at, deleted
                FROM eval_submission WHERE tenant_id = ? AND deleted = 0
                """, tenantId));
        data.put("scores", queryList("""
                SELECT id, tenant_id, school_id, submission_id, question_id, score,
                       created_at, updated_at, deleted
                FROM eval_score WHERE tenant_id = ? AND deleted = 0
                """, tenantId));
        data.put("answers", queryList("""
                SELECT id, tenant_id, school_id, submission_id, question_id, answer_value,
                       created_at, updated_at, deleted
                FROM eval_answer WHERE tenant_id = ? AND deleted = 0
                """, tenantId));
        data.put("questions", queryList("""
                SELECT id, tenant_id, school_id, form_id, type, title, required,
                       max_score, min_length, sort_order, created_at, updated_at, deleted
                FROM eval_question WHERE tenant_id = ? AND deleted = 0
                """, tenantId));
        data.put("options", queryList("""
                SELECT id, tenant_id, school_id, question_id, option_text, sort_order,
                       created_at, updated_at, deleted
                FROM eval_question_option WHERE tenant_id = ? AND deleted = 0
                """, tenantId));
        data.put("complaints", complaintRows(tenantId, null));
        data.put("records", queryList("""
                SELECT id, tenant_id, complaint_id, handler_id, from_status, to_status,
                       content, created_at, deleted
                FROM cmp_process_record WHERE tenant_id = ? AND deleted = 0
                """, tenantId));
        data.put("courses", queryList("""
                SELECT id, tenant_id, school_id, teaching_org_id, course_code, course_name,
                       term, start_at, end_at, created_at, updated_at, deleted
                FROM sch_course WHERE tenant_id = ? AND deleted = 0
                """, tenantId));
        data.put("courseTeachers", queryList("""
                SELECT id, tenant_id, school_id, course_id, teacher_id, role_status,
                       created_at, updated_at, deleted
                FROM sch_course_teacher WHERE tenant_id = ? AND deleted = 0
                """, tenantId));
        data.put("teachingOrgs", queryList("""
                SELECT id, tenant_id, school_id, parent_id, name, code, type, status,
                       created_at, updated_at, deleted
                FROM sch_teaching_org_unit WHERE tenant_id = ? AND deleted = 0
                """, tenantId));
        data.put("serviceItems", queryList("""
                SELECT id, tenant_id, school_id, service_org_id, name, cover_file_id,
                       type, status, created_at, updated_at, deleted
                FROM sch_service_item WHERE tenant_id = ? AND deleted = 0
                """, tenantId));
        data.put("serviceOrgs", queryList("""
                SELECT id, tenant_id, school_id, parent_id, name, code, type, status,
                       created_at, updated_at, deleted
                FROM sch_service_org_unit WHERE tenant_id = ? AND deleted = 0
                """, tenantId));
        return data;
    }

    @Override
    public Map<String, Object> todoStats() {
        Long tenantId = requireTenantId();
        long pendingForms = queryCount("""
                SELECT COUNT(*) FROM eval_form_publish_audit
                WHERE tenant_id = ? AND status = 'pending' AND deleted = 0
                """, tenantId);
        long pendingFeedback = queryCount("""
                SELECT COUNT(*) FROM cmp_complaint
                WHERE tenant_id = ? AND status IN ('pending','processing') AND deleted = 0
                """, tenantId);
        long activeWindows = queryCount("""
                SELECT COUNT(*) FROM eval_window
                WHERE tenant_id = ? AND status IN ('open','active') AND deleted = 0
                """, tenantId);
        Map<String, Object> data = new HashMap<>();
        data.put("pendingForms", pendingForms);
        data.put("pendingFeedback", pendingFeedback);
        data.put("activeWindows", activeWindows);
        data.put("pendingAppeals", 0);
        return data;
    }

    @Override
    public List<Map<String, Object>> activeWindows() {
        Long tenantId = requireTenantId();
        return queryList("""
                SELECT w.id, w.tenant_id, w.school_id, w.form_id, w.type, w.start_at, w.end_at,
                       w.modifiable_hours, w.status, w.created_at, w.updated_at, w.deleted,
                       COALESCE(f.title, CONCAT('评价表单 #', w.form_id)) AS form_title,
                       COALESCE(f.type, w.type) AS form_type,
                       COUNT(s.id) AS submission_count,
                       '' AS cover_url
                FROM eval_window w
                LEFT JOIN eval_form f ON f.id = w.form_id AND f.deleted = 0
                LEFT JOIN eval_submission s ON s.window_id = w.id AND s.deleted = 0
                WHERE w.tenant_id = ? AND w.status IN ('open','active') AND w.deleted = 0
                GROUP BY w.id, f.title, f.type
                ORDER BY w.start_at DESC
                LIMIT 10
                """, tenantId);
    }

    @Override
    public List<Map<String, Object>> pendingFeedback() {
        Long tenantId = requireTenantId();
        return queryList("""
                SELECT c.id, c.title,
                       CASE c.complaint_type
                         WHEN 'complaint' THEN '投诉'
                         WHEN 'suggestion' THEN '建议'
                         WHEN 'inquiry' THEN '咨询'
                         WHEN 'praise' THEN '表扬'
                         ELSE '反馈'
                       END AS type,
                       CASE c.status
                         WHEN 'pending' THEN '待处理'
                         WHEN 'processing' THEN '处理中'
                         WHEN 'resolved' THEN '已办结'
                         WHEN 'rejected' THEN '已驳回'
                         WHEN 'cancelled' THEN '已撤销'
                         ELSE c.status
                       END AS status,
                       c.status AS status_code,
                       COALESCE(si.name, so.name, co.course_name, tu.name, '其他') AS target_name,
                       COALESCE(MAX(r.created_at), c.updated_at) AS latest_process_time,
                       CASE WHEN c.anonymous_to_handler = 1 THEN '匿名' ELSE '学生用户' END AS submitter_label
                FROM cmp_complaint c
                LEFT JOIN cmp_process_record r ON r.complaint_id = c.id AND r.deleted = 0
                LEFT JOIN sch_service_item si ON si.id = c.service_item_id AND si.deleted = 0
                LEFT JOIN sch_service_org_unit so ON so.id = c.service_org_id AND so.deleted = 0
                LEFT JOIN sch_course co ON co.id = c.course_id AND co.deleted = 0
                LEFT JOIN sch_teaching_org_unit tu ON tu.id = c.teaching_org_id AND tu.deleted = 0
                WHERE c.tenant_id = ? AND c.status IN ('pending','processing') AND c.deleted = 0
                GROUP BY c.id, si.name, so.name, co.course_name, tu.name
                ORDER BY c.updated_at DESC
                LIMIT 5
                """, tenantId);
    }

    @Override
    public Map<String, Object> evaluationSummary() {
        Long tenantId = requireTenantId();
        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        long monthCount = queryCount("""
                SELECT COUNT(*) FROM eval_submission
                WHERE tenant_id = ? AND deleted = 0 AND status IN ('submitted','locked','reviewed')
                  AND submitted_at >= ?
                """, tenantId, monthStart);
        BigDecimal avgScore = queryDecimal("""
                SELECT AVG(score) FROM eval_score WHERE tenant_id = ? AND deleted = 0
                """, tenantId);
        long lowScoreCount = queryCount("""
                SELECT COUNT(DISTINCT submission_id) FROM eval_score
                WHERE tenant_id = ? AND deleted = 0 AND score <= 2
                """, tenantId);
        long submissions = queryCount("""
                SELECT COUNT(*) FROM eval_submission
                WHERE tenant_id = ? AND deleted = 0 AND status IN ('submitted','locked','reviewed')
                """, tenantId);
        Map<String, Object> data = new HashMap<>();
        data.put("monthCount", monthCount);
        data.put("avgScore", avgScore.setScale(1, RoundingMode.HALF_UP));
        data.put("participationRate", submissions > 0 ? Math.min(100, submissions * 5) : 0);
        data.put("lowScoreCount", lowScoreCount);
        return data;
    }

    private List<Map<String, Object>> complaintRows(Long tenantId, String statusFilter) {
        String statusSql = statusFilter == null ? "" : " AND c.status = '" + statusFilter + "'";
        return queryList("""
                SELECT c.id, c.tenant_id, c.school_id, c.submitter_id, c.complaint_type,
                       c.target_type, c.target_id, c.course_id, c.teaching_org_id,
                       c.service_item_id, c.service_org_id, c.title, c.content, c.status,
                       c.priority, c.anonymous_to_handler, c.attachment_file_ids,
                       c.cancelled_at, c.cancel_reason, c.resolved_at, c.created_at,
                       c.updated_at, c.deleted
                FROM cmp_complaint c WHERE c.tenant_id = ? AND c.deleted = 0
                """ + statusSql, tenantId);
    }

    private List<Map<String, Object>> queryList(String sql, Object... args) {
        try {
            return jdbcTemplate.queryForList(sql, args);
        } catch (DataAccessException ex) {
            log.debug("Stats query skipped: {}", ex.getMessage());
            return List.of();
        }
    }

    private long queryCount(String sql, Object... args) {
        try {
            Long value = jdbcTemplate.queryForObject(sql, Long.class, args);
            return value == null ? 0 : value;
        } catch (DataAccessException ex) {
            log.debug("Stats count skipped: {}", ex.getMessage());
            return 0;
        }
    }

    private BigDecimal queryDecimal(String sql, Object... args) {
        try {
            BigDecimal value = jdbcTemplate.queryForObject(sql, BigDecimal.class, args);
            return value == null ? BigDecimal.ZERO : value;
        } catch (DataAccessException ex) {
            log.debug("Stats decimal skipped: {}", ex.getMessage());
            return BigDecimal.ZERO;
        }
    }

    private Long requireTenantId() {
        Long tenantId = SecurityUtils.getTenantId();
        if (tenantId == null) {
            throw new BusinessException(403, "无法获取租户信息");
        }
        return tenantId;
    }
}
