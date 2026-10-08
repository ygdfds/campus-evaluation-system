package com.campus.evaluation.review.service;

import java.util.List;
import java.util.Map;

public interface ReviewService {

    List<Map<String, Object>> listAppeals(Map<String, Object> params);

    Map<String, Object> getAppeal(Long id);

    Map<String, Object> createAppeal(Map<String, Object> payload);

    Map<String, Object> updateAppeal(Long id, Map<String, Object> payload);

    List<Map<String, Object>> listAppealProcessRecords(Map<String, Object> params);

    Map<String, Object> createAppealProcessRecord(Map<String, Object> payload);

    List<Map<String, Object>> listTraceAuthorizations(Map<String, Object> params);

    Map<String, Object> getTraceAuthorization(Long id);

    Map<String, Object> createTraceAuthorization(Map<String, Object> payload);

    Map<String, Object> updateTraceAuthorization(Long id, Map<String, Object> payload);
}
