package com.whu.onlinechat.service;

import com.whu.onlinechat.common.BizException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.whu.onlinechat.dto.ChangePasswordRequest;
import com.whu.onlinechat.dto.UpdateProfileRequest;
import com.whu.onlinechat.entity.User;
import com.whu.onlinechat.mapper.UserMapper;
import com.whu.onlinechat.vo.UserVO;
import java.util.List;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class UserService {
    private static final long MAX_AVATAR_SIZE = 5L * 1024 * 1024;
    private static final Map<String, String> AVATAR_TYPES = Map.of(
        "image/jpeg", ".jpg",
        "image/png", ".png",
        "image/gif", ".gif",
        "image/webp", ".webp"
    );
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

    @Transactional
    public UserVO uploadAvatar(Long userId, MultipartFile file) {
        User user = requireActiveUser(userId);
        if (file == null || file.isEmpty()) {
            throw new BizException("请选择头像图片");
        }
        if (file.getSize() > MAX_AVATAR_SIZE) {
            throw new BizException("头像图片不能超过 5MB");
        }
        String extension = AVATAR_TYPES.get(file.getContentType());
        if (extension == null || !hasExpectedImageSignature(file, extension)) {
            throw new BizException("头像只支持 JPG、PNG、GIF 或 WebP 图片");
        }
        try {
            Path avatarDir = Path.of("uploads", "avatars").toAbsolutePath().normalize();
            Files.createDirectories(avatarDir);
            String filename = UUID.randomUUID() + extension;
            file.transferTo(avatarDir.resolve(filename));
            user.setAvatarUrl("/api/users/avatars/" + filename);
            userMapper.updateById(user);
            return UserVO.from(user);
        } catch (IOException ex) {
            throw new BizException("头像保存失败");
        }
    }

    public AvatarFile loadAvatar(String filename) {
        if (filename == null || !filename.matches("[0-9a-fA-F-]+\\.(jpg|png|gif|webp)")) {
            throw new BizException("头像不存在");
        }
        try {
            Path path = Path.of("uploads", "avatars", filename).toAbsolutePath().normalize();
            Resource resource = new UrlResource(path.toUri());
            if (!resource.exists()) {
                throw new BizException("头像不存在");
            }
            String contentType = Files.probeContentType(path);
            return new AvatarFile(contentType == null ? "application/octet-stream" : contentType, resource);
        } catch (IOException ex) {
            throw new BizException("头像读取失败");
        }
    }

    public record AvatarFile(String contentType, Resource resource) {
    }

    private boolean hasExpectedImageSignature(MultipartFile file, String extension) {
        try (InputStream input = file.getInputStream()) {
            byte[] bytes = input.readNBytes(12);
            return switch (extension) {
                case ".jpg" -> bytes.length >= 3
                    && (bytes[0] & 0xff) == 0xff && (bytes[1] & 0xff) == 0xd8 && (bytes[2] & 0xff) == 0xff;
                case ".png" -> bytes.length >= 8
                    && (bytes[0] & 0xff) == 0x89 && bytes[1] == 0x50 && bytes[2] == 0x4e && bytes[3] == 0x47;
                case ".gif" -> bytes.length >= 6 && new String(bytes, 0, 3).equals("GIF");
                case ".webp" -> bytes.length >= 12
                    && new String(bytes, 0, 4).equals("RIFF") && new String(bytes, 8, 4).equals("WEBP");
                default -> false;
            };
        } catch (IOException ex) {
            return false;
        }
    }
}
