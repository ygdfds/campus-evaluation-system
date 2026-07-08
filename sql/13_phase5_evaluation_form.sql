-- Phase 5: 评价表单管理 - 新增字段
ALTER TABLE eval_form ADD COLUMN score_enabled TINYINT(1) NOT NULL DEFAULT 0 AFTER anonymous;
ALTER TABLE eval_form ADD COLUMN published_at DATETIME NULL DEFAULT NULL AFTER status;
