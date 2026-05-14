package com.whu.onlinechat.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class AdminLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long adminId;
    private String operationType;
    private Long targetUserId;
    private String content;
    private LocalDateTime createTime;
}
