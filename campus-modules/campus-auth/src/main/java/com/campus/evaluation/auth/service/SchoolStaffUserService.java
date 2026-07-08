package com.campus.evaluation.auth.service;

import com.campus.evaluation.auth.domain.dto.AssignRolesDTO;
import com.campus.evaluation.auth.domain.dto.ChangeUserStatusDTO;
import com.campus.evaluation.auth.domain.dto.ResetPasswordDTO;
import com.campus.evaluation.auth.domain.dto.StaffUserCreateDTO;
import com.campus.evaluation.auth.domain.dto.StaffUserUpdateDTO;
import com.campus.evaluation.auth.domain.vo.StaffUserVO;
import com.campus.evaluation.auth.domain.vo.UserDetailVO;
import com.campus.evaluation.common.core.domain.PageResult;

public interface SchoolStaffUserService {

    PageResult<StaffUserVO> list(String keyword, String status, Long teachingOrgId, Long serviceOrgId,
                                  String roleCode, int pageNum, int pageSize);

    UserDetailVO getById(Long id);

    StaffUserVO create(StaffUserCreateDTO dto);

    StaffUserVO update(Long id, StaffUserUpdateDTO dto);

    void changeStatus(Long id, ChangeUserStatusDTO dto);

    void resetPassword(Long id, ResetPasswordDTO dto);

    void assignRoles(Long id, AssignRolesDTO dto);
}
