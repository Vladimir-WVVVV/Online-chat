package com.whu.onlinechat.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.whu.onlinechat.common.BizException;
import com.whu.onlinechat.entity.Notification;
import com.whu.onlinechat.mapper.NotificationMapper;
import com.whu.onlinechat.vo.NotificationVO;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationMapper notificationMapper;
    private final SimpMessagingTemplate messagingTemplate;

    public NotificationVO create(Long receiverId, String type, String content) {
        Notification notification = new Notification();
        notification.setReceiverId(receiverId);
        notification.setType(type);
        notification.setContent(content);
        notification.setReadFlag(0);
        notificationMapper.insert(notification);
        NotificationVO vo = toVO(notification);
        messagingTemplate.convertAndSendToUser(String.valueOf(receiverId), "/queue/notifications", vo);
        return vo;
    }

    public List<NotificationVO> list(Long userId) {
        return notificationMapper.selectList(new LambdaQueryWrapper<Notification>()
                .eq(Notification::getReceiverId, userId)
                .orderByDesc(Notification::getCreateTime))
            .stream().map(this::toVO).toList();
    }

    public Long unreadCount(Long userId) {
        return notificationMapper.selectCount(new LambdaQueryWrapper<Notification>()
            .eq(Notification::getReceiverId, userId)
            .eq(Notification::getReadFlag, 0));
    }

    @Transactional
    public void read(Long userId, Long id) {
        int updated = notificationMapper.update(new Notification(), new LambdaUpdateWrapper<Notification>()
            .eq(Notification::getId, id)
            .eq(Notification::getReceiverId, userId)
            .set(Notification::getReadFlag, 1));
        if (updated == 0) {
            throw new BizException("通知不存在");
        }
    }

    public void readAll(Long userId) {
        notificationMapper.update(new Notification(), new LambdaUpdateWrapper<Notification>()
            .eq(Notification::getReceiverId, userId)
            .set(Notification::getReadFlag, 1));
    }

    public void readByTypeAndKeyword(Long userId, String type, String keyword) {
        notificationMapper.update(new Notification(), new LambdaUpdateWrapper<Notification>()
            .eq(Notification::getReceiverId, userId)
            .eq(Notification::getType, type)
            .like(Notification::getContent, keyword)
            .set(Notification::getReadFlag, 1));
    }

    private NotificationVO toVO(Notification n) {
        return new NotificationVO(n.getId(), n.getType(), n.getContent(), n.getReadFlag() != null && n.getReadFlag() == 1,
            n.getCreateTime());
    }
}
