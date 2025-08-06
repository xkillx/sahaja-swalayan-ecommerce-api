package com.sahaja.swalayan.ecommerce.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "biteship")
public class BiteshipProperties {
    private String apiKey;
    private String baseUrl;
}
