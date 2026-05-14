package com.whu.onlinechat.vo;

import com.whu.onlinechat.entity.User;

public record UserVO(
    Long id,
    String username,
    String email,
    String nickname,
    String avatarUrl,
    String bio,
    String status,
    String role
) {
    public static UserVO from(User user) {
        return new UserVO(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getNickname(),
            user.getAvatarUrl(),
            user.getBio(),
            user.getStatus(),
            user.getRole()
        );
    }
}
