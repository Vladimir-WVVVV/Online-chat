package com.whu.onlinechat.mapper;

import com.whu.onlinechat.entity.FriendRequest;
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
@DisplayName("FriendRequestMapper 集成测试")
class FriendRequestMapperTest {

    @Autowired
    private FriendRequestMapper mapper;

    @Test
    @DisplayName("插入并查询好友申请")
    void shouldInsertAndSelect() {
        var req = new FriendRequest();
        req.setFromUserId(1L);
        req.setToUserId(2L);
        req.setStatus("PENDING");
        req.setMessage("Hello");

        mapper.insert(req);
        assertThat(req.getId()).isNotNull();

        var found = mapper.selectById(req.getId());
        assertThat(found.getStatus()).isEqualTo("PENDING");
        assertThat(found.getMessage()).isEqualTo("Hello");
    }

    @Test
    @DisplayName("按状态查询申请")
    void shouldQueryByStatus() {
        var req = new FriendRequest();
        req.setFromUserId(10L);
        req.setToUserId(20L);
        req.setStatus("PENDING");
        mapper.insert(req);

        var wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<FriendRequest>()
            .eq(FriendRequest::getStatus, "PENDING")
            .eq(FriendRequest::getFromUserId, 10L);
        var results = mapper.selectList(wrapper);
        assertThat(results).isNotEmpty();
    }
}
