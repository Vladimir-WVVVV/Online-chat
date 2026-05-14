package com.whu.onlinechat.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.whu.onlinechat.common.BizException;
import com.whu.onlinechat.entity.AdminLog;
import com.whu.onlinechat.entity.Message;
import com.whu.onlinechat.entity.User;
import com.whu.onlinechat.mapper.AdminLogMapper;
import com.whu.onlinechat.mapper.MessageMapper;
import com.whu.onlinechat.mapper.UserMapper;
import com.whu.onlinechat.vo.AdminMetricsVO;
import com.whu.onlinechat.vo.UserVO;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final UserMapper userMapper;
    private final MessageMapper messageMapper;
    private final AdminLogMapper adminLogMapper;
    private final OnlineUserService onlineUserService;

    public List<UserVO> users() {
        return userMapper.selectList(new LambdaQueryWrapper<User>().orderByDesc(User::getCreateTime))
            .stream().map(UserVO::from).toList();
    }

    @Transactional
    public void ban(Long adminId, Long userId) {
        updateStatus(adminId, userId, "BANNED", "BAN_USER");
    }

    @Transactional
    public void unban(Long adminId, Long userId) {
        updateStatus(adminId, userId, "OFFLINE", "UNBAN_USER");
    }

    public AdminMetricsVO metrics() {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        Long messages = messageMapper.selectCount(new LambdaQueryWrapper<Message>().ge(Message::getCreateTime, start));
        Long users = userMapper.selectCount(new LambdaQueryWrapper<User>().ge(User::getCreateTime, start));
        return new AdminMetricsVO(onlineUserService.onlineCount(), messages, users);
    }

    public List<AdminLog> logs() {
        return adminLogMapper.selectList(new LambdaQueryWrapper<AdminLog>().orderByDesc(AdminLog::getCreateTime).last("limit 100"));
    }

    private void updateStatus(Long adminId, Long userId, String status, String operation) {
        User target = userMapper.selectById(userId);
        if (target == null) {
            throw new BizException("用户不存在");
        }
        if ("ADMIN".equals(target.getRole())) {
            throw new BizException("不能封禁管理员");
        }
        target.setStatus(status);
        userMapper.updateById(target);
        AdminLog log = new AdminLog();
        log.setAdminId(adminId);
        log.setTargetUserId(userId);
        log.setOperationType(operation);
        log.setContent("用户 " + userId + " 状态改为 " + status);
        adminLogMapper.insert(log);
    }
}
