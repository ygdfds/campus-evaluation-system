package com.campus.evaluation.auth.service;

import com.campus.evaluation.auth.domain.dto.AdminUserCreateDTO;
import com.campus.evaluation.auth.domain.dto.AdminUserUpdateDTO;
import com.campus.evaluation.auth.domain.dto.ChangeUserStatusDTO;
import com.campus.evaluation.auth.domain.dto.ResetPasswordDTO;
import com.campus.evaluation.auth.domain.vo.AdminUserVO;
import com.campus.evaluation.auth.domain.vo.UserDetailVO;
import com.campus.evaluation.common.core.domain.PageResult;

public interface SchoolAdminUserService {

    PageResult<AdminUserVO> list(String keyword, String status, int pageNum, int pageSize);

    UserDetailVO getById(Long id);

    AdminUserVO create(AdminUserCreateDTO dto);

    AdminUserVO update(Long id, AdminUserUpdateDTO dto);

    void changeStatus(Long id, ChangeUserStatusDTO dto);

    void resetPassword(Long id, ResetPasswordDTO dto);
}
