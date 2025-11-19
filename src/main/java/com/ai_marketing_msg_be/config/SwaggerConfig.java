package com.ai_marketing_msg_be.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("MIXOLOGY - AI Marketing Message API")
                        .description("생성형 AI 기반 초개인화 마케팅 메시지 자동 생성 솔루션 API")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("MIXOLOGY Team")
                                .email("contact@mixology.com")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Local Development Server"),
                        new Server()
                                .url("https://api.mixology.com")
                                .description("Production Server")
                ));
    }
}
