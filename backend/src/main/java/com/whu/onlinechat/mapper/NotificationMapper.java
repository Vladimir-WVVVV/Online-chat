package com.whu.onlinechat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.whu.onlinechat.entity.Notification;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {
}
