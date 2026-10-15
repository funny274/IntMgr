package com.example.test1;

import com.example.test1.converter.UserConverter;
import com.example.test1.enums.UserStatusEnum;
import com.example.test1.model.dto.UserCreateDTO;
import com.example.test1.model.dto.UserUpdateDTO;
import com.example.test1.model.entity.UserEntity;
import com.example.test1.model.vo.UserVO;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 【单元测试】用户转换器。
 * <p>
 * 转换器是纯逻辑、无依赖，最适合练单元测试。覆盖：
 *   ① 创建默认状态 ② 状态码 → 中文描述 ③ 部分更新只拷非空字段。
 */
class UserConverterTest {

    @Test
    void shouldDefaultStatusToActiveOnCreate() {
        UserCreateDTO dto = UserCreateDTO.builder().username("zhangsan").build();

        UserEntity entity = UserConverter.toEntity(dto);

        assertThat(entity.getStatus()).isEqualTo(UserStatusEnum.ACTIVE.getCode());
        assertThat(entity.getUsername()).isEqualTo("zhangsan");
    }

    @Test
    void shouldTranslateStatusCodeToDesc() {
        UserEntity entity = new UserEntity();
        entity.setId(1L);
        entity.setUsername("lisi");
        entity.setStatus(UserStatusEnum.INACTIVE.getCode());

        UserVO vo = UserConverter.toVO(entity);

        assertThat(vo.getStatusDesc()).isEqualTo("停用");
        assertThat(vo.getStatus()).isEqualTo("INACTIVE");
    }

    @Test
    void shouldOnlyApplyNonNullFieldsOnUpdate() {
        UserEntity entity = new UserEntity();
        entity.setNickname("旧昵称");
        entity.setEmail("old@example.com");
        entity.setAge(20);

        UserConverter.applyUpdate(entity, UserUpdateDTO.builder().age(30).build());

        assertThat(entity.getAge()).isEqualTo(30);          // 传了值的被更新
        assertThat(entity.getNickname()).isEqualTo("旧昵称"); // 没传的保持原值
        assertThat(entity.getEmail()).isEqualTo("old@example.com");
    }

}