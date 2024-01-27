package com.jumani.rutaseg.filter;

import com.jumani.rutaseg.exception.ForbiddenException;
import com.jumani.rutaseg.exception.InvalidRequestOriginException;
import com.jumani.rutaseg.exception.UnauthorizedException;
import com.jumani.rutaseg.service.auth.JwtService;
import com.jumani.rutaseg.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Order(2)
@AllArgsConstructor
@Slf4j
public class SessionFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final List<String> knownOrigins;
    private final boolean allowAllOrigins;

    public static final String ACCESS_CONTROL_REQUEST_HEADERS = "access-control-request-headers";
    public static final String ORIGIN_HEADER = "x-auth-origin";

    // Endpoints que NO necesitan ser autorizados con JWT
    private static final List<String> SKIPPED_ENDPOINTS;
    protected static final List<String> ADMIN_ENDPOINTS;


    static {
        SKIPPED_ENDPOINTS = new ArrayList<>();
        SKIPPED_ENDPOINTS.add("/login");
        SKIPPED_ENDPOINTS.add("/internal/send-email");
    }

    static {
        ADMIN_ENDPOINTS = new ArrayList<>();
    }

    /*
     * Este método se ejecuta en todas las peticiones antes de llegar al controller correspondiente.
     * Cuando se trata de un endpoint que no está en la lista 'skippedEndpoints', lo que hace es extraer el JWT del header y verificar su validez.
     * Si es nulo/vacío/inválido/vencido arroja un error.
     * Si es válido llama al filterChain.doFilter() lo cual significa seguir el curso natural de la petición (ir al controller)
     */
    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request,
                                    @NotNull HttpServletResponse httpServletResponse,
                                    FilterChain filterChain) throws ServletException, IOException {

        this.validateSession(request);
        filterChain.doFilter(request, httpServletResponse);
    }

    private void validateSession(HttpServletRequest request) {
        if (this.isPreFlight(request)) return;

        if (this.isHealthCheck(request)) return;

        if (!this.isValidRequestOrigin(request)) {
            throw new InvalidRequestOriginException();
        }

        final String endpoint = request.getRequestURI();
        if (SKIPPED_ENDPOINTS.contains(endpoint)) return;

        final String token = JwtUtil.extractToken(request).orElseThrow(UnauthorizedException::new);
        if (!jwtService.isTokenValid(token)) {
            throw new UnauthorizedException();
        }

        if (ADMIN_ENDPOINTS.stream().anyMatch(endpoint::startsWith) && !jwtService.isAdminToken(token)) {
            throw new ForbiddenException();
        }
    }

    private boolean isHealthCheck(HttpServletRequest request) {
        return "/".equals(request.getRequestURI()) && "GET".equalsIgnoreCase(request.getMethod());
    }

    private boolean isPreFlight(HttpServletRequest request) {
        return request.getHeader(ACCESS_CONTROL_REQUEST_HEADERS) != null;
    }

    private boolean isValidRequestOrigin(HttpServletRequest request) {
        return allowAllOrigins || Optional.ofNullable(request.getHeader(ORIGIN_HEADER))
                .map(knownOrigins::contains)
                .orElse(false);
    }
}
