package com.whu.onlinechat.service;

import com.whu.onlinechat.common.BizException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.whu.onlinechat.dto.ChangePasswordRequest;
import com.whu.onlinechat.dto.UpdateProfileRequest;
import com.whu.onlinechat.entity.User;
import com.whu.onlinechat.mapper.UserMapper;
import com.whu.onlinechat.vo.UserVO;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserVO getProfile(Long userId) {
        User user = requireActiveUser(userId);
        return UserVO.from(user);
    }

    @Transactional
    public UserVO updateProfile(Long userId, UpdateProfileRequest request) {
        User user = requireActiveUser(userId);
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

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = requireActiveUser(userId);
        if (!passwordEncoder.matches(request.oldPassword(), user.getPasswordHash())) {
            throw new BizException("原密码错误");
        }
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userMapper.updateById(user);
    }

    public List<UserVO> search(Long currentUserId, String keyword) {
        requireActiveUser(currentUserId);
        String text = keyword == null ? "" : keyword.trim();
        return userMapper.selectList(new LambdaQueryWrapper<User>()
                .ne(User::getId, currentUserId)
                .ne(User::getRole, "AI")
                .and(!text.isBlank(), w -> w.like(User::getUsername, text).or().like(User::getNickname, text))
                .last("limit 20"))
            .stream().map(UserVO::from).toList();
    }

    public User requireActiveUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException("用户不存在");
        }
        if ("BANNED".equals(user.getStatus())) {
            throw new BizException("账号已被封禁");
        }
        return user;
    }
}
