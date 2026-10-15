package com.example.test1.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.test1.model.entity.UserEntity;

/**
 * ====================================================================================
 * 【持久层】UserMapper —— 用户表的数据访问接口
 * ====================================================================================
 *
 * ▌1. 继承 BaseMapper 后白捡的能力：
 *   insert / deleteById / updateById / selectById / selectList / selectPage / selectCount ...
 *   全部无需写一句 SQL。这就是 MyBatis-Plus 相对原生 MyBatis 最大的省事之处。
 *
 * ▌2. 要写复杂 SQL 怎么办？
 *   两种方式（本案例用不到，先了解）：
 *     - 注解方式：@Select("SELECT ... FROM T_USER WHERE ...")
 *     - XML 方式：在 resources/mapper/UserMapper.xml 里写 <select>，最灵活（动态 SQL 复杂场景）
 *   MyBatis-Plus 不阻拦你写原生 SQL，只是通用 CRUD 让你"少写 80% 的 SQL"。
 *
 * ▌3. 接口为什么不写 @Mapper？
 *   因为 MybatisPlusConfig 上已经 @MapperScan("com.example.test1.mapper") 统一扫描注册了。
 * ====================================================================================
 */
public interface UserMapper extends BaseMapper<UserEntity> {
}