package com.campus.evaluation.message.service;

import java.util.List;
import java.util.Map;

public interface HelpCenterService {

    List<Map<String, Object>> listGuides(Map<String, Object> params);

    List<Map<String, Object>> listFaqs(Map<String, Object> params);

    List<Map<String, Object>> listTickets(Map<String, Object> params);

    Map<String, Object> createTicket(Map<String, Object> payload);

    Map<String, Object> updateTicket(Long id, Map<String, Object> payload);
}
