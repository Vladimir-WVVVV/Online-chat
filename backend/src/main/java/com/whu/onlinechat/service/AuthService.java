package com.whu.onlinechat.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.whu.onlinechat.common.BizException;
import com.whu.onlinechat.dto.LoginRequest;
import com.whu.onlinechat.dto.RegisterRequest;
import com.whu.onlinechat.entity.User;
import com.whu.onlinechat.mapper.UserMapper;
import com.whu.onlinechat.security.CurrentUser;
import com.whu.onlinechat.security.JwtService;
import com.whu.onlinechat.vo.LoginVO;
import com.whu.onlinechat.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional
    public UserVO register(RegisterRequest request) {
        Long usernameCount = userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getUsername, request.username()));
        if (usernameCount > 0) {
            throw new BizException("用户名已存在");
        }
        Long emailCount = userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getEmail, request.email()));
        if (emailCount > 0) {
            throw new BizException("邮箱已存在");
        }
        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setNickname(request.username());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole("USER");
        user.setStatus("OFFLINE");
        userMapper.insert(user);
        return UserVO.from(user);
    }

    public LoginVO login(LoginRequest request) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, request.username()));
        if (user == null || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("用户名或密码错误");
        }
        if ("BANNED".equals(user.getStatus())) {
            throw new BizException("账号已被封禁");
        }
        String token = jwtService.generate(new CurrentUser(user.getId(), user.getUsername(), user.getRole()));
        return new LoginVO(token, UserVO.from(user));
    }

    public UserVO me(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException("用户不存在");
        }
        return UserVO.from(user);
    }
}
