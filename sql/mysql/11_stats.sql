USE campus_evaluation_system;

-- Statistics snapshots. These tables are produced by scheduled jobs or analytics jobs.

CREATE TABLE IF NOT EXISTS stat_tenant_snapshot (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '租户统计快照ID',
  tenant_id BIGINT NOT NULL COMMENT '租户ID',
  school_id BIGINT NULL COMMENT '学校ID',
  stat_date DATE NOT NULL COMMENT '统计日期',
  metric_key VARCHAR(120) NOT NULL COMMENT '指标Key',
  metric_value DECIMAL(18,4) NOT NULL DEFAULT 0 COMMENT '指标值',
  stat_data JSON NULL COMMENT '扩展统计JSON',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_stat_tenant_metric (tenant_id, stat_date, metric_key),
  KEY idx_stat_tenant_date (tenant_id, stat_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='租户统计快照';

CREATE TABLE IF NOT EXISTS stat_service_snapshot (
  id BIGINT NOT NULL AUTO_INCREMENT COMMENT '服务统计快照ID',
  tenant_id BIGINT NOT NULL COMMENT '租户ID',
  school_id BIGINT NULL COMMENT '学校ID',
  service_item_id BIGINT NOT NULL COMMENT '服务项目ID',
  stat_date DATE NOT NULL COMMENT '统计日期',
  score_avg DECIMAL(5,2) NULL COMMENT '平均评分',
  participation_rate DECIMAL(6,2) NULL COMMENT '参与率',
  keywords_json JSON NULL COMMENT '关键词JSON',
  stat_data JSON NULL COMMENT '扩展统计JSON',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_stat_service_date (tenant_id, service_item_id, stat_date),
  KEY idx_stat_service_tenant_date (tenant_id, stat_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='服务统计快照';
