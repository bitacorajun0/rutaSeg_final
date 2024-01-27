package com.jumani.rutaseg.service.auth;

import java.util.UUID;

public class JwtServiceDev implements JwtService {
    @Override
    public String generateToken(String subject, boolean admin) {
        return UUID.randomUUID().toString();
    }

    @Override
    public String extractSubject(String token) {
        return String.valueOf(token);
    }

    @Override
    public boolean isTokenValid(String token) {
        return true;
    }

    @Override
    public boolean isAdminToken(String token) {
        return true;
    }
}
