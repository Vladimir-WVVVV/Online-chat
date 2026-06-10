package com.whu.onlinechat.mapper;

import com.whu.onlinechat.entity.Notification;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("NotificationMapper 集成测试")
class NotificationMapperTest {

    @Autowired
    private NotificationMapper mapper;

    @Test
    @DisplayName("插入并查询通知")
    void shouldInsertAndSelect() {
        var n = new Notification();
        n.setReceiverId(1L);
        n.setType("FRIEND_REQUEST");
        n.setContent("用户 2 请求添加你为好友");
        n.setReadFlag(0);

        mapper.insert(n);
        assertThat(n.getId()).isNotNull();

        var found = mapper.selectById(n.getId());
        assertThat(found.getReadFlag()).isEqualTo(0);
        assertThat(found.getType()).isEqualTo("FRIEND_REQUEST");
    }

    @Test
    @DisplayName("按接收者查询未读通知")
    void shouldQueryUnreadByReceiver() {
        var n = new Notification();
        n.setReceiverId(50L);
        n.setType("PRIVATE_MESSAGE");
        n.setContent("test");
        n.setReadFlag(0);
        mapper.insert(n);

        var wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Notification>()
            .eq(Notification::getReceiverId, 50L)
            .eq(Notification::getReadFlag, 0);
        var results = mapper.selectList(wrapper);
        assertThat(results).isNotEmpty();
        assertThat(results.get(0).getType()).isEqualTo("PRIVATE_MESSAGE");
    }
}
