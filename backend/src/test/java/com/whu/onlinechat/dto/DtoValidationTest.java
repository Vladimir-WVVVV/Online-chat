package com.whu.onlinechat.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DTO Validation 测试")
class DtoValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Nested
    @DisplayName("LoginRequest")
    class LoginRequestValidation {

        @Test
        @DisplayName("所有字段非空时校验通过")
        void shouldPassWithValidInput() {
            LoginRequest req = new LoginRequest("alice", "123456");
            var violations = validator.validate(req);
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("用户名为空时校验失败")
        void shouldFailWhenUsernameBlank() {
            LoginRequest req = new LoginRequest("", "123456");
            var violations = validator.validate(req);
            assertThat(violations).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("RegisterRequest")
    class RegisterRequestValidation {

        @Test
        @DisplayName("合法输入校验通过")
        void shouldPassWithValidInput() {
            RegisterRequest req = new RegisterRequest("alice", "alice@test.com", "123456");
            var violations = validator.validate(req);
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("用户名过短时校验失败")
        void shouldFailWhenUsernameTooShort() {
            RegisterRequest req = new RegisterRequest("ab", "ab@test.com", "123456");
            var violations = validator.validate(req);
            assertThat(violations).isNotEmpty();
        }

        @Test
        @DisplayName("邮箱格式错误时校验失败")
        void shouldFailWhenEmailInvalid() {
            RegisterRequest req = new RegisterRequest("newuser", "not-an-email", "123456");
            var violations = validator.validate(req);
            assertThat(violations).isNotEmpty();
        }

        @Test
        @DisplayName("密码过短时校验失败")
        void shouldFailWhenPasswordTooShort() {
            RegisterRequest req = new RegisterRequest("newuser", "new@test.com", "12345");
            var violations = validator.validate(req);
            assertThat(violations).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("ChangePasswordRequest")
    class ChangePasswordRequestValidation {

        @Test
        @DisplayName("合法输入校验通过")
        void shouldPassWithValidInput() {
            ChangePasswordRequest req = new ChangePasswordRequest("old123", "new123");
            var violations = validator.validate(req);
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("新密码过短时校验失败")
        void shouldFailWhenNewPasswordTooShort() {
            ChangePasswordRequest req = new ChangePasswordRequest("old123", "12345");
            var violations = validator.validate(req);
            assertThat(violations).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("CreateGroupRequest")
    class CreateGroupRequestValidation {

        @Test
        @DisplayName("群名为空时校验失败")
        void shouldFailWhenNameBlank() {
            CreateGroupRequest req = new CreateGroupRequest("  ", null, null, null);
            var violations = validator.validate(req);
            assertThat(violations).isNotEmpty();
        }

        @Test
        @DisplayName("群名过长时校验失败")
        void shouldFailWhenNameTooLong() {
            String longName = "a".repeat(81);
            CreateGroupRequest req = new CreateGroupRequest(longName, null, null, null);
            var violations = validator.validate(req);
            assertThat(violations).isNotEmpty();
        }
    }
}
