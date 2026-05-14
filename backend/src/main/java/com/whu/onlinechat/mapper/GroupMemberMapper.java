package com.whu.onlinechat.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.whu.onlinechat.entity.GroupMember;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface GroupMemberMapper extends BaseMapper<GroupMember> {
}
