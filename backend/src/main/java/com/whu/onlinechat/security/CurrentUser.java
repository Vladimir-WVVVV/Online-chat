package com.whu.onlinechat.security;

public record CurrentUser(Long id, String username, String role) {
}

