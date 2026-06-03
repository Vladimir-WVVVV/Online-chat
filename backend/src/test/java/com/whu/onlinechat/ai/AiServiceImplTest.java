package com.whu.onlinechat.ai;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class AiServiceImplTest {
    @Test
    void usesHttpProviderOnlyWhenProviderIsHttp() {
        assertThat(AiServiceImpl.useHttpProvider("http")).isTrue();
        assertThat(AiServiceImpl.useHttpProvider(" HTTP ")).isTrue();
    }

    @Test
    void usesMockWhenProviderIsMock() {
        assertThat(AiServiceImpl.useHttpProvider("mock")).isFalse();
        assertThat(AiServiceImpl.useHttpProvider(null)).isFalse();
    }
}
