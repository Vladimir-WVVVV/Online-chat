package com.whu.onlinechat.common;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("BizException 单元测试")
class BizExceptionTest {

    @Test
    @DisplayName("创建业务异常并获取消息")
    void shouldCreateWithMessage() {
        BizException ex = new BizException("用户名已存在");

        assertThat(ex).isInstanceOf(RuntimeException.class);
        assertThat(ex.getMessage()).isEqualTo("用户名已存在");
    }
}
