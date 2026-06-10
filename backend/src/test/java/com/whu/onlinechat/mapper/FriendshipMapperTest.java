package com.whu.onlinechat.mapper;

import com.whu.onlinechat.entity.Friendship;
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
@DisplayName("FriendshipMapper 集成测试")
class FriendshipMapperTest {

    @Autowired
    private FriendshipMapper friendshipMapper;

    @Test
    @DisplayName("插入并查询好友关系")
    void shouldInsertAndSelectFriendship() {
        Friendship fs = new Friendship();
        fs.setUserId(1L);
        fs.setFriendId(2L);
        fs.setStatus("ACTIVE");
        fs.setRemark("Best friend");

        friendshipMapper.insert(fs);
        assertThat(fs.getId()).isNotNull();

        Friendship found = friendshipMapper.selectById(fs.getId());
        assertThat(found.getUserId()).isEqualTo(1L);
        assertThat(found.getFriendId()).isEqualTo(2L);
        assertThat(found.getRemark()).isEqualTo("Best friend");
    }

    @Test
    @DisplayName("插入并查询多对好友关系")
    void shouldInsertMultipleFriendships() {
        // Use unique IDs to avoid unique constraint (user_id, friend_id) from first test
        for (int i = 10; i <= 13; i++) {
            Friendship fs = new Friendship();
            fs.setUserId(100L);
            fs.setFriendId((long) i);
            fs.setStatus("ACTIVE");
            friendshipMapper.insert(fs);
        }

        var wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Friendship>()
            .eq(Friendship::getUserId, 100L);
        var results = friendshipMapper.selectList(wrapper);
        assertThat(results).hasSize(4);
    }
}
