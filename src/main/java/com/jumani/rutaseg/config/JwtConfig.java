package com.jumani.rutaseg.config;

import com.jumani.rutaseg.service.auth.JwtService;
import com.jumani.rutaseg.service.auth.JwtServiceDev;
import com.jumani.rutaseg.service.auth.JwtServiceImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class JwtConfig {

    @Bean
    @Profile("!local & !integration_test")
    public JwtService jwtService(@Value("${jwt.secret-key}") String secretKey) {
        return new JwtServiceImpl(secretKey);
    }

    @Bean
    @Profile("local | integration_test")
    public JwtService jwtServiceDev() {
        return new JwtServiceDev();
    }
}
