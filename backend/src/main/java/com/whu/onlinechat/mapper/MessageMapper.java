package com.whu.onlinechat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.whu.onlinechat.entity.Message;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MessageMapper extends BaseMapper<Message> {
}
