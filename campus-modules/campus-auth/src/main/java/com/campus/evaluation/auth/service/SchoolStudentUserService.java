package com.campus.evaluation.auth.service;

import com.campus.evaluation.auth.domain.dto.ChangeUserStatusDTO;
import com.campus.evaluation.auth.domain.dto.ResetPasswordDTO;
import com.campus.evaluation.auth.domain.dto.StudentUserCreateDTO;
import com.campus.evaluation.auth.domain.dto.StudentUserUpdateDTO;
import com.campus.evaluation.auth.domain.vo.StudentUserVO;
import com.campus.evaluation.auth.domain.vo.UserDetailVO;
import com.campus.evaluation.common.core.domain.PageResult;

public interface SchoolStudentUserService {

    PageResult<StudentUserVO> list(String keyword, String status, Long classId, String grade,
                                    int pageNum, int pageSize);

    UserDetailVO getById(Long id);

    StudentUserVO create(StudentUserCreateDTO dto);

    StudentUserVO update(Long id, StudentUserUpdateDTO dto);

    void changeStatus(Long id, ChangeUserStatusDTO dto);

    void resetPassword(Long id, ResetPasswordDTO dto);
}
