package com.example.test1.converter;

import com.example.test1.enums.UserStatusEnum;
import com.example.test1.model.dto.UserCreateDTO;
import com.example.test1.model.dto.UserUpdateDTO;
import com.example.test1.model.entity.UserEntity;
import com.example.test1.model.vo.UserVO;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 【转换器】实体 / DTO / VO 之间的"翻译官"。
 * <p>
 * 与 GreetingConverter 相同的是工具类写法（final + 私有构造）。
 * 区别：这里因为字段多、映射关系多，把三个方向的转换集中放在一起，避免散落到 Service 各处。
 * 提示：这类机械映射，真实工程常用 MapStruct 自动生成，本项目手写以降低学习门槛。
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UserConverter {

    /**
     * 创建入参 → 实体。创建时状态固定为 ACTIVE，时间由 MyMetaObjectHandler 自动填。
     */
    public static UserEntity toEntity(UserCreateDTO dto) {
        UserEntity entity = new UserEntity();
        entity.setUsername(dto.getUsername());
        entity.setNickname(dto.getNickname());
        entity.setEmail(dto.getEmail());
        entity.setAge(dto.getAge());
        entity.setStatus(UserStatusEnum.ACTIVE.getCode());
        return entity;
    }

    /**
     * 实体 → 响应 VO，附带把状态码翻译成中文描述。
     */
    public static UserVO toVO(UserEntity entity) {
        if (entity == null) {
            return null;
        }
        UserStatusEnum statusEnum = UserStatusEnum.of(entity.getStatus());
        return UserVO.builder()
                .id(entity.getId())
                .username(entity.getUsername())
                .nickname(entity.getNickname())
                .email(entity.getEmail())
                .age(entity.getAge())
                .status(entity.getStatus())
                .statusDesc(statusEnum == null ? entity.getStatus() : statusEnum.getDesc())
                .createdTime(entity.getCreatedTime())
                .updatedTime(entity.getUpdatedTime())
                .build();
    }

    /**
     * 把更新 DTO 中"非空"的字段拷贝到实体（实现部分更新）。
     * 只改传了值的字段，没传的保持原值。配合 MyBatis-Plus 的"非空才更新"策略。
     */
    public static void applyUpdate(UserEntity target, UserUpdateDTO dto) {
        if (dto.getNickname() != null) {
            target.setNickname(dto.getNickname());
        }
        if (dto.getEmail() != null) {
            target.setEmail(dto.getEmail());
        }
        if (dto.getAge() != null) {
            target.setAge(dto.getAge());
        }
        if (dto.getStatus() != null) {
            target.setStatus(dto.getStatus());
        }
    }

}