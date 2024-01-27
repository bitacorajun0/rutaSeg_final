package com.jumani.rutaseg.config;

import com.jumani.rutaseg.handler.SessionInfoHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final SessionInfoHandler sessionInfoHandler;

    private final List<String> allowedOriginPatterns;

    public WebConfig(SessionInfoHandler sessionInfoHandler,
                     @Value("${web.cors.allowed-origin-patterns}") List<String> allowedOriginPatterns) {
        this.sessionInfoHandler = sessionInfoHandler;
        this.allowedOriginPatterns = allowedOriginPatterns;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(sessionInfoHandler);
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns(allowedOriginPatterns.toArray(new String[0]))
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowCredentials(true);
    }
}