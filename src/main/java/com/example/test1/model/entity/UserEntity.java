package com.example.test1.model.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * ====================================================================================
 * 【实体】UserEntity —— 对应数据库表 T_USER 的"映射对象"
 * ====================================================================================
 *
 * ▌1. Entity 与 DTO/VO 的区别（前面 DTO/VO 讲过，这里对照强调）：
 *   - Entity 字段和数据库列一一对应，一步到位映射；
 *   - DTO 是"收进来的参数"，VO 是"发出去的结果"；
 *   - 三层各司其职，永不互相乱用，这是分层架构的底线。
 *
 * ▌2. 类上/字段上的 MyBatis-Plus 注解：
 *
 *   @TableName("T_USER")  指定这张实体对应哪张表（省略则按类名驼峰转下划线）。
 *
 *   @TableId(type = IdType.ASSIGN_ID)
 *     主键策略：ASSIGN_ID = MyBatis-Plus 用雪花算法自动生成 19 位 Long 主键。
 *     好处：无需依赖 Oracle 序列/自增，分布式环境下主键也全局唯一。
 *
 *   @TableField("USERNAME") 显式声明字段对应哪一列。
 *     为什么每个都显式写？Oracle 列默认大写，显式映射能避免大小写歧义，也更好读。
 *
 *   @TableLogic 逻辑删除：执行 delete 时不真删数据，而是把 deleted 从 0 改成 1。
 *     注意：MyBatis-Plus 所有查询会自动带上 deleted=0 条件，被逻辑删的数据查不到。
 *
 *   @TableField(fill = FieldFill.INSERT)  表示该字段在 INSERT 时由 MyMetaObjectHandler 自动填。
 * ====================================================================================
 */
@Data
@TableName("T_USER")
public class UserEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 主键（雪花算法自动生成） */
    @TableId(value = "ID", type = IdType.ASSIGN_ID)
    private Long id;

    /** 登录名 * */
    @TableField("USERNAME")
    private String username;

    /** 昵称 */
    @TableField("NICKNAME")
    private String nickname;

    /** 邮箱 */
    @TableField("EMAIL")
    private String email;

    /** 年龄 */
    @TableField("AGE")
    private Integer age;

    /** 状态码：ACTIVE / INACTIVE */
    @TableField("STATUS")
    private String status;

    /** 逻辑删除标记：0 正常 / 1 删除（@TableLogic 自动维护） */
    @TableLogic
    @TableField("DELETED")
    private Integer deleted;

    /** 创建时间（INSERT 时自动填充） */
    @TableField(value = "CREATED_TIME", fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    /** 更新时间（INSERT/UPDATE 时自动填充） */
    @TableField(value = "UPDATED_TIME", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;

}