package com.gaia3d.mago.server.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Slf4j
@Configuration
public class ResourceConfiguration implements WebMvcConfigurer {

    private final String STATIC_PATH = "/static/**";
    private final String ASSET_EXTERNAL_PATH = "/data/**";

    @Value("${asset.path}")
    private String ASSET_PATH;

    @Value("${asset.allows.domain}")
    private String ALLOW_DOMAIN_PATTERNS;

    /*@Value("${data.path}")
    private String DATA_PATH;*/

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        String assetPath = replacePathSuffix(ASSET_PATH);
        assetPath = replacePathPrefix(assetPath);

        log.info("Resource Path: {}", STATIC_PATH);
        registry.addResourceHandler(STATIC_PATH)
                .addResourceLocations("classpath:/static/");

        log.info("Resource Path: {}", assetPath);
        registry.addResourceHandler(ASSET_EXTERNAL_PATH)
                .addResourceLocations(assetPath);
    }

    @Override
    public void addCorsMappings(final CorsRegistry registry) {
        registry.addMapping(ASSET_EXTERNAL_PATH)
                .allowedOriginPatterns(ALLOW_DOMAIN_PATTERNS)
                .allowedMethods("GET")
                .allowCredentials(true).maxAge(3600);
    }

    private String replacePathPrefix(String path) {
        if (path.charAt(0) == '/') {
            return "file:" + path;
        } else {
            return "file:///" + path;
        }
    }

    private String replacePathSuffix(String path) {
        if (!path.endsWith("/")) {
            path += "/";
        }
        return path;
    }
}
