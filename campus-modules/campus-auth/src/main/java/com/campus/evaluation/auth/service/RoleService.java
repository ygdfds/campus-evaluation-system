package com.campus.evaluation.auth.service;

import com.campus.evaluation.auth.domain.vo.RoleOptionVO;

import java.util.List;

public interface RoleService {

    List<RoleOptionVO> getOptions(String userType);
}
