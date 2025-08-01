package com.sahaja.swalayan.ecommerce.infrastructure.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "xendit")
@Getter
@Setter
public class XenditProperties {
    private String apiKey;
    private String baseUrl;
    private String successRedirectUrl;
    private String callbackToken;
}
