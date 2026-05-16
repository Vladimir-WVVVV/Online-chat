package com.whu.onlinechat.ai;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.whu.onlinechat.entity.User;
import com.whu.onlinechat.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class AiVirtualUserInitializer implements CommandLineRunner {
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        for (AiAgentType agentType : AiAgentType.values()) {
            ensureAgent(agentType);
        }
    }

    private void ensureAgent(AiAgentType agentType) {
        Long count = userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getUsername, agentType.username()));
        if (count != null && count > 0) {
            return;
        }
        User user = new User();
        user.setUsername(agentType.username());
        user.setEmail(agentType.username() + "@onlinechat.local");
        user.setPasswordHash(passwordEncoder.encode("AI_USER_NOT_FOR_LOGIN"));
        user.setNickname(agentType.displayName());
        user.setBio("OnlineChat AI virtual agent");
        user.setRole("AI");
        user.setStatus("ONLINE");
        userMapper.insert(user);
    }
}
