package com.whu.onlinechat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.whu.onlinechat.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}

