package com.whu.onlinechat.common;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ApiResult 单元测试")
class ApiResultTest {

    @Nested
    @DisplayName("success")
    class Success {

        @Test
        @DisplayName("success(T data) 返回 code=0")
        void shouldReturnSuccessWithData() {
            ApiResult<String> result = ApiResult.success("hello");

            assertThat(result.code()).isEqualTo(0);
            assertThat(result.message()).isEqualTo("success");
            assertThat(result.data()).isEqualTo("hello");
        }

        @Test
        @DisplayName("success() 无参返回 code=0 data=null")
        void shouldReturnSuccessWithoutData() {
            ApiResult<Void> result = ApiResult.success();

            assertThat(result.code()).isEqualTo(0);
            assertThat(result.message()).isEqualTo("success");
            assertThat(result.data()).isNull();
        }
    }

    @Nested
    @DisplayName("error")
    class Error {

        @Test
        @DisplayName("error(message) 返回 code=1")
        void shouldReturnErrorWithMessage() {
            ApiResult<Void> result = ApiResult.error("出错了");

            assertThat(result.code()).isEqualTo(1);
            assertThat(result.message()).isEqualTo("出错了");
            assertThat(result.data()).isNull();
        }
    }
}
