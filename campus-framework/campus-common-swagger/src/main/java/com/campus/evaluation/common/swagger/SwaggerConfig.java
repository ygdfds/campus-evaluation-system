package com.campus.evaluation.common.swagger;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Springdoc / Knife4j 接口文档配置
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("校园服务质量在线评测系统 API")
                        .version("1.0.0")
                        .description("校园服务质量在线评测系统后端接口文档")
                        .contact(new Contact().name("Campus Evaluation Team")));
    }
}
