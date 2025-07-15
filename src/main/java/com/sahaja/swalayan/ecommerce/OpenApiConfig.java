package com.sahaja.swalayan.ecommerce;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Sahaja Swalayan Ecommerce API")
                        .version("1.0.0")
                        .description("OpenAPI documentation for Sahaja Swalayan Ecommerce Backend API."));
    }
}
