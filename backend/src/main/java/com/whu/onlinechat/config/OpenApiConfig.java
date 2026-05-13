package com.whu.onlinechat.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    OpenAPI openAPI() {
        return new OpenAPI().info(new Info()
            .title("OnlineChat API")
            .version("0.1.0")
            .description("武汉大学软件工程课程项目 OnlineChat 接口文档"));
    }
}

