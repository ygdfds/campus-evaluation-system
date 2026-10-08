package com.campus.evaluation.message.service;

import java.util.List;
import java.util.Map;

public interface NotificationService {

    List<Map<String, Object>> list(Map<String, Object> params);

    Map<String, Object> get(Long id);

    Map<String, Object> create(Map<String, Object> payload);

    Map<String, Object> update(Long id, Map<String, Object> payload);
}
