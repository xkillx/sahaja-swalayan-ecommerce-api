package com.sahaja.swalayan.ecommerce.infrastructure.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${server.servlet.context-path:/api}")
    private String contextPath;

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Sahaja Swalayan Ecommerce API")
                        .description("REST API for Sahaja Swalayan Ecommerce Platform")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Sahaja Swalayan Development Team")
                                .email("dev@sahajaswalayan.com")
                                .url("https://sahajaswalayan.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort + contextPath)
                                .description("Development Server"),
                        new Server()
                                .url("https://api.sahajaswalayan.com")
                                .description("Production Server")));
    }
}
