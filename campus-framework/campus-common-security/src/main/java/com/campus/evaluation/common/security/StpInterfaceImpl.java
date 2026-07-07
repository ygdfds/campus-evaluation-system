package com.campus.evaluation.common.security;

import cn.dev33.satoken.stp.StpInterface;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Sa-Token 权限数据加载实现
 * <p>
 * Sa-Token 在执行权限校验时自动调用此 Bean 获取当前用户的角色和权限列表
 */
@Component
public class StpInterfaceImpl implements StpInterface {

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return SecurityUtils.getPermissions();
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        return SecurityUtils.getRoles();
    }
}
