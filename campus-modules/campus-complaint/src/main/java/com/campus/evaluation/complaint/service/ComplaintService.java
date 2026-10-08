package com.campus.evaluation.complaint.service;

import java.util.List;
import java.util.Map;

public interface ComplaintService {

    List<Map<String, Object>> listComplaints(Map<String, Object> params);

    Map<String, Object> getComplaint(Long id);

    Map<String, Object> createComplaint(Map<String, Object> payload);

    Map<String, Object> updateComplaint(Long id, Map<String, Object> payload);

    List<Map<String, Object>> listProcessRecords(Map<String, Object> params);

    Map<String, Object> createProcessRecord(Map<String, Object> payload);

    List<Map<String, Object>> listWorkOrders(Map<String, Object> params);

    Map<String, Object> createWorkOrder(Map<String, Object> payload);

    Map<String, Object> updateWorkOrder(Long id, Map<String, Object> payload);
}
