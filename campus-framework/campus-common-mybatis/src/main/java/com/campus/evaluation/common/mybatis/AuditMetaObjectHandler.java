package com.campus.evaluation.common.mybatis;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 自动填充处理器（createdAt / updatedAt）
 */
@Slf4j
@Component
public class AuditMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        log.debug("自动填充 INSERT 字段");
        this.strictInsertFill(metaObject, "createdAt", LocalDateTime::now, LocalDateTime.class);
        this.strictInsertFill(metaObject, "updatedAt", LocalDateTime::now, LocalDateTime.class);
        this.strictInsertFill(metaObject, "createdBy", () -> "system", String.class);
        this.strictInsertFill(metaObject, "updatedBy", () -> "system", String.class);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        log.debug("自动填充 UPDATE 字段");
        this.strictUpdateFill(metaObject, "updatedAt", LocalDateTime::now, LocalDateTime.class);
        this.strictUpdateFill(metaObject, "updatedBy", () -> "system", String.class);
    }
}
