package com.campus.evaluation.common.tenant;

import java.io.Serial;
import java.io.Serializable;

import lombok.Data;

/**
 * 租户上下文信息
 */
@Data
public class TenantContext implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 租户ID */
    private Long tenantId;

    /** 学校ID */
    private Long schoolId;
}
