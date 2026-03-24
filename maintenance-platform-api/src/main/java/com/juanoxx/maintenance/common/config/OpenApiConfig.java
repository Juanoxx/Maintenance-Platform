package com.juanoxx.maintenance.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI maintenanceOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Maintenance Platform API")
                        .description("API for building maintenance incident lifecycle management")
                        .version("v1")
                        .contact(new Contact().name("Maintenance Team")));
    }
}
