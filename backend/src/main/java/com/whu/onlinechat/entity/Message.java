package com.whu.onlinechat.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class Message {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String conversationType;
    private Long senderId;
    private Long receiverId;
    private Long groupId;
    private String content;
    private String messageType;
    private Long fileId;
    private Integer recalled;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    @TableLogic
    private Integer deleted;
}
