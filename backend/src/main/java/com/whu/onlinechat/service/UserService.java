package com.whu.onlinechat.service;

import com.whu.onlinechat.common.BizException;
import com.whu.onlinechat.dto.UpdateProfileRequest;
import com.whu.onlinechat.entity.User;
import com.whu.onlinechat.mapper.UserMapper;
import com.whu.onlinechat.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserMapper userMapper;

    public UserVO getProfile(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException("用户不存在");
        }
        return UserVO.from(user);
    }

    @Transactional
    public UserVO updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException("用户不存在");
        }
        if (request.nickname() != null && !request.nickname().isBlank()) {
            user.setNickname(request.nickname());
        }
        if (request.avatarUrl() != null) {
            user.setAvatarUrl(request.avatarUrl());
        }
        if (request.bio() != null) {
            user.setBio(request.bio());
        }
        userMapper.updateById(user);
        return UserVO.from(user);
    }
}

