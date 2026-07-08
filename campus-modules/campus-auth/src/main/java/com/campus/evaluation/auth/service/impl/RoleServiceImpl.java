package com.campus.evaluation.auth.service.impl;

import com.campus.evaluation.auth.domain.vo.RoleOptionVO;
import com.campus.evaluation.auth.service.RoleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class RoleServiceImpl implements RoleService {

    // 可分配角色定义（硬编码，不允许 school_admin 分配 system_admin）
    private static final List<RoleOptionVO> ADMIN_ROLES = List.of(
            RoleOptionVO.builder().roleCode("school_admin").roleName("学校管理员").build()
    );

    private static final List<RoleOptionVO> STAFF_ROLES = List.of(
            RoleOptionVO.builder().roleCode("staff").roleName("教职工").build(),
            RoleOptionVO.builder().roleCode("teaching_admin").roleName("学院教学管理员").build(),
            RoleOptionVO.builder().roleCode("service_admin").roleName("后勤部门管理员").build(),
            RoleOptionVO.builder().roleCode("feedback_handler").roleName("反馈处理员").build(),
            RoleOptionVO.builder().roleCode("form_publisher").roleName("评价表单发布员").build()
    );

    private static final List<RoleOptionVO> STUDENT_ROLES = List.of(
            RoleOptionVO.builder().roleCode("student").roleName("学生").build()
    );

    @Override
    public List<RoleOptionVO> getOptions(String userType) {
        if (userType == null || userType.isEmpty()) {
            // 返回所有可分配角色（不含 system_admin）
            return concatAll();
        }
        return switch (userType) {
            case "admin" -> ADMIN_ROLES;
            case "staff" -> STAFF_ROLES;
            case "student" -> STUDENT_ROLES;
            default -> concatAll();
        };
    }

    private List<RoleOptionVO> concatAll() {
        return java.util.stream.Stream.of(ADMIN_ROLES, STAFF_ROLES, STUDENT_ROLES)
                .flatMap(List::stream)
                .toList();
    }
}
