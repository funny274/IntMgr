-- ====================================================================================
-- init.sql —— Oracle 建表脚本（学习案例）
-- ====================================================================================
-- 使用方法：用 SQL 工具（PL/SQL Developer / SQLcl / DBeaver）连上目标库后执行本文件。
-- 注意：同一用户下重复执行会报"名称已由现有对象使用"，可先 DROP TABLE T_USER 再执行。
-- ====================================================================================

-- 用户表：演示 MyBatis-Plus 的 CRUD + 分页 + 逻辑删除 + 自动填充
CREATE TABLE T_USER (
    ID            NUMBER(19)      NOT NULL,            -- 主键，雪花算法生成的 19 位 Long
    USERNAME      VARCHAR2(50)    NOT NULL,            -- 登录名/帐号，唯一
    NICKNAME      VARCHAR2(100),                       -- 昵称
    EMAIL         VARCHAR2(100),                       -- 邮箱
    AGE           NUMBER(3),                           -- 年龄
    STATUS        VARCHAR2(20)    DEFAULT 'ACTIVE' NOT NULL, -- 状态：ACTIVE=启用 / INACTIVE=停用
    DELETED       NUMBER(1)       DEFAULT 0 NOT NULL,  -- 逻辑删除标记：0=正常 / 1=已删除
    CREATED_TIME  DATE,                                -- 创建时间（MetaObjectHandler 自动填充）
    UPDATED_TIME  DATE,                                -- 更新时间（自动填充）
    CONSTRAINT PK_T_USER PRIMARY KEY (ID),
    CONSTRAINT UK_T_USER_USERNAME UNIQUE (USERNAME)
);

-- 表/字段注释（Oracle 元数据，便于团队理解表结构）
COMMENT ON TABLE  T_USER             IS '用户表';
COMMENT ON COLUMN T_USER.ID          IS '主键 ID（雪花算法）';
COMMENT ON COLUMN T_USER.USERNAME    IS '登录名（唯一）';
COMMENT ON COLUMN T_USER.NICKNAME    IS '昵称';
COMMENT ON COLUMN T_USER.EMAIL       IS '邮箱';
COMMENT ON COLUMN T_USER.AGE         IS '年龄';
COMMENT ON COLUMN T_USER.STATUS      IS '状态：ACTIVE=启用/INACTIVE=停用';
COMMENT ON COLUMN T_USER.DELETED     IS '逻辑删除标记：0=正常/1=已删除';
COMMENT ON COLUMN T_USER.CREATED_TIME IS '创建时间';
COMMENT ON COLUMN T_USER.UPDATED_TIME IS '更新时间';

-- 造几条初始数据，方便接口联调（雪花 ID 手动给固定值即可）
INSERT INTO T_USER (ID, USERNAME, NICKNAME, EMAIL, AGE, STATUS, DELETED, CREATED_TIME, UPDATED_TIME)
VALUES (1001, 'zhangsan', '张三', 'zhangsan@example.com', 28, 'ACTIVE', 0, SYSDATE, SYSDATE);

INSERT INTO T_USER (ID, USERNAME, NICKNAME, EMAIL, AGE, STATUS, DELETED, CREATED_TIME, UPDATED_TIME)
VALUES (1002, 'lisi', '李四', 'lisi@example.com', 32, 'ACTIVE', 0, SYSDATE, SYSDATE);

INSERT INTO T_USER (ID, USERNAME, NICKNAME, EMAIL, AGE, STATUS, DELETED, CREATED_TIME, UPDATED_TIME)
VALUES (1003, 'wangwu', '王五', 'wangwu@example.com', 35, 'INACTIVE', 0, SYSDATE, SYSDATE);

COMMIT;