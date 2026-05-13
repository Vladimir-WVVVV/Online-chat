package com.whu.onlinechat.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "onlinechat.jwt")
public record JwtProperties(String secret, long expireMinutes) {
}

