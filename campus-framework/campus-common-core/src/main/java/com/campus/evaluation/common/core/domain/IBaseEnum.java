package com.campus.evaluation.common.core.domain;

/**
 * 枚举基础接口
 * <p>
 * 所有业务枚举应实现此接口，统一获取 code 和 description
 *
 * @param <T> 枚举 code 类型
 */
public interface IBaseEnum<T> {

    /**
     * 获取枚举 code
     */
    T getCode();

    /**
     * 获取枚举描述
     */
    String getDescription();
}
