package com.whu.onlinechat.common;

import com.whu.onlinechat.dto.RegisterRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import jakarta.validation.ConstraintViolationException;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("GlobalExceptionHandler 单元测试")
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Nested
    @DisplayName("BizException")
    class BizExceptionHandler {

        @Test
        @DisplayName("返回 400 和业务错误消息")
        void shouldReturn400WithMessage() {
            BizException ex = new BizException("用户名已存在");

            ApiResult<Void> result = handler.handleBiz(ex);

            assertThat(result.code()).isEqualTo(1);
            assertThat(result.message()).isEqualTo("用户名已存在");
        }
    }

    @Nested
    @DisplayName("认证异常")
    class AuthExceptionHandler {

        @Test
        @DisplayName("BadCredentialsException 返回错误消息")
        void shouldHandleBadCredentials() {
            BadCredentialsException ex = new BadCredentialsException("用户名或密码错误");

            ApiResult<Void> result = handler.handleAuth(ex);

            assertThat(result.code()).isEqualTo(1);
            assertThat(result.message()).isEqualTo("用户名或密码错误");
        }

        @Test
        @DisplayName("AccessDeniedException 返回错误消息")
        void shouldHandleAccessDenied() {
            AccessDeniedException ex = new AccessDeniedException("权限不足");

            ApiResult<Void> result = handler.handleAuth(ex);

            assertThat(result.code()).isEqualTo(1);
            assertThat(result.message()).isEqualTo("权限不足");
        }
    }

    @Nested
    @DisplayName("类型不匹配异常")
    class TypeMismatchHandler {

        @Test
        @DisplayName("返回参数格式错误")
        void shouldReturnTypeMismatchError() {
            MethodArgumentTypeMismatchException ex = new MethodArgumentTypeMismatchException(
                null, null, "id", null, null);

            ApiResult<Void> result = handler.handleTypeMismatch(ex);

            assertThat(result.message()).isEqualTo("参数格式错误");
        }
    }

    @Nested
    @DisplayName("通用异常")
    class GeneralHandler {

        @Test
        @DisplayName("未知异常返回服务器内部错误")
        void shouldReturn500() {
            Exception ex = new Exception("unknown");

            ApiResult<Void> result = handler.handleOther(ex);

            assertThat(result.code()).isEqualTo(1);
            assertThat(result.message()).isEqualTo("服务器内部错误");
        }
    }
}
