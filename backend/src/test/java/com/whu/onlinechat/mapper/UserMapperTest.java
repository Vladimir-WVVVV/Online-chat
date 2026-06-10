package com.whu.onlinechat.mapper;

import com.whu.onlinechat.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("UserMapper 集成测试")
class UserMapperTest {

    @Autowired
    private UserMapper userMapper;

    @Nested
    @DisplayName("CRUD 操作")
    class Crud {

        @Test
        @DisplayName("插入并查询用户")
        void shouldInsertAndSelect() {
            User user = new User();
            user.setUsername("testuser");
            user.setEmail("test@test.com");
            user.setPasswordHash("hashed");
            user.setNickname("Test");
            user.setRole("USER");
            user.setStatus("OFFLINE");

            int rows = userMapper.insert(user);
            assertThat(rows).isEqualTo(1);
            assertThat(user.getId()).isNotNull();

            User found = userMapper.selectById(user.getId());
            assertThat(found).isNotNull();
            assertThat(found.getUsername()).isEqualTo("testuser");
        }

        @Test
        @DisplayName("根据用户名查询用户")
        void shouldSelectByUsername() {
            User user = new User();
            user.setUsername("byusername");
            user.setEmail("byusername@test.com");
            user.setPasswordHash("hashed");
            user.setNickname("ByUsername");
            user.setRole("USER");
            user.setStatus("OFFLINE");
            userMapper.insert(user);

            var wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                .eq(User::getUsername, "byusername");
            User found = userMapper.selectOne(wrapper);
            assertThat(found).isNotNull();
            assertThat(found.getEmail()).isEqualTo("byusername@test.com");
        }

        @Test
        @DisplayName("更新用户信息")
        void shouldUpdateUser() {
            User user = new User();
            user.setUsername("updateuser");
            user.setEmail("update@test.com");
            user.setPasswordHash("hashed");
            user.setNickname("Old");
            user.setRole("USER");
            user.setStatus("OFFLINE");
            userMapper.insert(user);

            user.setNickname("New");
            userMapper.updateById(user);

            User updated = userMapper.selectById(user.getId());
            assertThat(updated.getNickname()).isEqualTo("New");
        }

        @Test
        @DisplayName("查询不存在的用户返回 null")
        void shouldReturnNullForMissingUser() {
            User found = userMapper.selectById(99999L);
            assertThat(found).isNull();
        }
    }
}
