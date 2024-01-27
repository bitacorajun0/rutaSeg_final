package com.jumani.rutaseg.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

public class JwtUtil {
    public static final String AUTHORIZATION_HEADER = "x-auth-token";
    public static final String JWT_TOKEN_COOKIE_NAME = "jwtToken";
    public static final String BEARER_SUFFIX = "Bearer ";

    public static Optional<String> extractToken(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(AUTHORIZATION_HEADER))
                .filter(ah -> ah.startsWith(BEARER_SUFFIX))
                .map(ah -> ah.substring(BEARER_SUFFIX.length()))
                .or(() -> {
                    final Cookie[] cookies = request.getCookies();
                    if (Objects.isNull(cookies)) {
                        return Optional.empty();
                    }

                    return Arrays.stream(cookies)
                            .filter(cookie -> cookie.getName().equals(JWT_TOKEN_COOKIE_NAME))
                            .map(Cookie::getValue)
                            .findFirst();
                });
    }
}
