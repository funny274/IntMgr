package com.example.test1.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * ====================================================================================
 * 【自动填充】MyMetaObjectHandler —— 审计字段（createdTime/updatedTime）自动写入
 * ====================================================================================
 *
 * ▌1. 解决什么问题？
 *   每张表几乎都有 create_time / update_time 这类"审计字段"。
 *   如果每个 Service 插入前都手动 setCreatedTime(new Date())，代码又臭又容易漏。
 *   这里集中处理：只要实体字段标了 fill=INSERT / INSERT_UPDATE，
 *   执行 insert/update 时本类会自动补上时间，业务代码完全不关心。
 *
 * ▌2. 两个回调方法：
 *   - insertFill：执行 INSERT 时触发（补 createdTime 和 updatedTime）
 *   - updateFill：执行 UPDATE 时触发（只补 updatedTime）
 *
 * ▌3. strictInsertFill 的"strict"含义：
 *   只有当目标字段当前是 null 时才填充；若业务已手动赋值则尊重业务值、不覆盖。
 *
 * ▌4. 配合哪个注解？
 *   实体字段上要标 @TableField(fill = FieldFill.INSERT / INSERT_UPDATE)，见 UserEntity。
 * ====================================================================================
 */
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();
        // 字段名要和实体里的驼峰属性名一致
        this.strictInsertFill(metaObject, "createdTime", LocalDateTime.class, now);
        this.strictInsertFill(metaObject, "updatedTime", LocalDateTime.class, now);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updatedTime", LocalDateTime.class, LocalDateTime.now());
    }

}