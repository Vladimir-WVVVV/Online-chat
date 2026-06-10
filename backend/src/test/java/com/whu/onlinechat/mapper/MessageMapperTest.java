package com.whu.onlinechat.mapper;

import com.whu.onlinechat.entity.Message;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("MessageMapper 集成测试")
class MessageMapperTest {

    @Autowired
    private MessageMapper messageMapper;

    @Nested
    @DisplayName("消息 CRUD")
    class MessageCrud {

        @Test
        @DisplayName("插入并查询消息")
        void shouldInsertAndSelect() {
            Message msg = new Message();
            msg.setConversationType("PRIVATE");
            msg.setSenderId(1L);
            msg.setReceiverId(2L);
            msg.setContent("Hello");
            msg.setMessageType("TEXT");
            msg.setRecalled(0);

            int rows = messageMapper.insert(msg);
            assertThat(rows).isEqualTo(1);
            assertThat(msg.getId()).isNotNull();

            Message found = messageMapper.selectById(msg.getId());
            assertThat(found.getContent()).isEqualTo("Hello");
            assertThat(found.getConversationType()).isEqualTo("PRIVATE");
        }

        @Test
        @DisplayName("插入群聊消息")
        void shouldInsertGroupMessage() {
            Message msg = new Message();
            msg.setConversationType("GROUP");
            msg.setSenderId(1L);
            msg.setGroupId(100L);
            msg.setContent("Group hello");
            msg.setMessageType("TEXT");
            msg.setRecalled(0);

            messageMapper.insert(msg);

            Message found = messageMapper.selectById(msg.getId());
            assertThat(found.getGroupId()).isEqualTo(100L);
            assertThat(found.getConversationType()).isEqualTo("GROUP");
        }

        @Test
        @DisplayName("按会话类型查询消息")
        void shouldQueryByConversationType() {
            Message msg1 = new Message();
            msg1.setConversationType("PRIVATE");
            msg1.setSenderId(1L);
            msg1.setReceiverId(2L);
            msg1.setContent("PM");
            msg1.setMessageType("TEXT");
            msg1.setRecalled(0);
            messageMapper.insert(msg1);

            Message msg2 = new Message();
            msg2.setConversationType("GROUP");
            msg2.setSenderId(1L);
            msg2.setGroupId(100L);
            msg2.setContent("GM");
            msg2.setMessageType("TEXT");
            msg2.setRecalled(0);
            messageMapper.insert(msg2);

            var wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Message>()
                .eq(Message::getConversationType, "PRIVATE");
            var results = messageMapper.selectList(wrapper);
            assertThat(results).allMatch(m -> "PRIVATE".equals(m.getConversationType()));
        }
    }
}
