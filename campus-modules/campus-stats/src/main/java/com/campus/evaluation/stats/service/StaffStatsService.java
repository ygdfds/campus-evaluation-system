package com.campus.evaluation.stats.service;

import java.util.List;
import java.util.Map;

public interface StaffStatsService {

    Map<String, Object> reportContext();

    Map<String, Object> todoStats();

    List<Map<String, Object>> activeWindows();

    List<Map<String, Object>> pendingFeedback();

    List<Map<String, Object>> pendingAppeals();

    Map<String, Object> evaluationSummary();
}
