package com.whu.onlinechat;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.whu.onlinechat.entity.User;
import com.whu.onlinechat.mapper.UserMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class OnlineChatApplication {
    public static void main(String[] args) {
        SpringApplication.run(OnlineChatApplication.class, args);
    }

    @Bean
    CommandLineRunner seedUsers(UserMapper userMapper, PasswordEncoder passwordEncoder) {
        return args -> {
            List<SeedUser> seeds = List.of(
                new SeedUser("admin", "admin@onlinechat.local", "管理员", "ADMIN"),
                new SeedUser("alice", "alice@onlinechat.local", "Alice", "USER"),
                new SeedUser("bob", "bob@onlinechat.local", "Bob", "USER"),
                new SeedUser("carol", "carol@onlinechat.local", "Carol", "USER")
            );
            for (SeedUser seed : seeds) {
                Long count = userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getUsername, seed.username()));
                if (count == 0) {
                    User user = new User();
                    user.setUsername(seed.username());
                    user.setEmail(seed.email());
                    user.setNickname(seed.nickname());
                    user.setRole(seed.role());
                    user.setStatus("OFFLINE");
                    user.setPasswordHash(passwordEncoder.encode("123456"));
                    userMapper.insert(user);
                }
            }
        };
    }

    private record SeedUser(String username, String email, String nickname, String role) {
    }
}
