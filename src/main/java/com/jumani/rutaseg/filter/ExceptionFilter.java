package com.jumani.rutaseg.filter;

import com.jumani.rutaseg.dto.result.Error;
import com.jumani.rutaseg.handler.ControllerExceptionHandler;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.http.entity.ContentType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
@Order(1)
public class ExceptionFilter extends OncePerRequestFilter {

    private final ControllerExceptionHandler exceptionHandler;

    public ExceptionFilter() {
        this.exceptionHandler =  new ControllerExceptionHandler();
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            final ResponseEntity<Error> error = exceptionHandler.handleException(e);
            HttpHeaders headers = new HttpHeaders();
            headers.add("Access-Control-Allow-Origin", request.getHeader("origin"));
            headers.add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            headers.add("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
            headers.add("Access-Control-Allow-Credentials", "true");

            headers.forEach((key, value) -> response.setHeader(key, value.get(0)));

            response.setStatus(error.getStatusCode().value());
            response.setContentType(ContentType.APPLICATION_JSON.getMimeType());
            response.getWriter().write(Optional.ofNullable(error.getBody()).map(Error::serialize).orElse(""));
            response.getWriter().flush();
        }
    }
}
