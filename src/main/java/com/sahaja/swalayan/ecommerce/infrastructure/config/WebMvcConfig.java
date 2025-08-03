package com.sahaja.swalayan.ecommerce.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.nio.file.Paths;
import java.io.File;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${app.upload.dir:uploads/products}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String absoluteUploadDir = Paths.get(uploadDir).toAbsolutePath().toString();
        String resourceLocation = "file:" + (absoluteUploadDir.endsWith(File.separator) ? absoluteUploadDir : absoluteUploadDir + File.separator);
        log.debug("[WebMvcConfig] Mapping /uploads/products/** to {}", resourceLocation);
        registry.addResourceHandler("/uploads/products/**")
                .addResourceLocations(resourceLocation);
    }
}
