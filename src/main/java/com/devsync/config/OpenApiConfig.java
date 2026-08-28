package com.devsync.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI devSyncOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("DevSync API")
                        .description("Developer Accountability and Growth Platform REST API")
                        .version("v0.0.1")
                        .contact(new Contact()
                                .name("DevSync Team")
                                .email("support@devsync.com")));
    }
}
