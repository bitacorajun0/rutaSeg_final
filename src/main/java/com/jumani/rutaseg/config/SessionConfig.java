package com.jumani.rutaseg.config;

import com.jumani.rutaseg.filter.SessionFilter;
import com.jumani.rutaseg.service.auth.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SessionConfig {

    @Bean
    public SessionFilter sessionFilter(JwtService jwtService,
                                       @Value("${session.known-origins}") List<String> knownOrigins,
                                       @Value("${session.allow-all-origins}") boolean allowAllOrigins) {
        return new SessionFilter(jwtService, knownOrigins, allowAllOrigins);
    }
}
