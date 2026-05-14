package com.whu.onlinechat.websocket;

import java.security.Principal;

public record UserPrincipal(Long id, String username, String role) implements Principal {
    @Override
    public String getName() {
        return String.valueOf(id);
    }
}
