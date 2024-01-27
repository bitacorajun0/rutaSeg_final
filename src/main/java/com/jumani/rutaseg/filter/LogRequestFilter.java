package com.jumani.rutaseg.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static java.util.stream.Collectors.joining;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(0)
public class LogRequestFilter extends OncePerRequestFilter {

    private final ObjectMapper mapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        MDC.put("requestId", UUID.randomUUID().toString());

        final ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
        final ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        final long startTime = System.currentTimeMillis();
        filterChain.doFilter(requestWrapper, responseWrapper);
        final long endTime = System.currentTimeMillis();

        final long elapsedMillis = endTime - startTime;

        final String url = this.buildUrl(requestWrapper);

        log.info(this.buildLog(url, this.requestBody(requestWrapper), this.buildRequestTags(requestWrapper)));
        log.info(this.buildLog(url, this.responseBody(responseWrapper), this.buildResponseTags(requestWrapper, responseWrapper, elapsedMillis)));

        responseWrapper.copyBodyToResponse();
    }

    private String buildUrl(final ContentCachingRequestWrapper request) {
        final String queryParams = Objects.nonNull(request.getQueryString()) ? "?" + request.getQueryString() : "";

        return request.getRequestURI() + queryParams;
    }

    private String buildLog(final String url, final Object body, final Map<String, Object> tags) {
        final String logTags = tags.entrySet().stream().map(this::buildLogTag).collect(joining(" "));

        return String.format("%s url: %s body: %s", logTags, url, body);
    }

    private Object requestBody(final ContentCachingRequestWrapper request) throws IOException {
        return this.readBody(request.getContentAsByteArray(), request.getCharacterEncoding());
    }

    private Object responseBody(final ContentCachingResponseWrapper response) throws IOException {
        return this.readBody(response.getContentAsByteArray(), response.getCharacterEncoding());
    }

    private Object readBody(final byte[] bytes, final String encoding) throws IOException {
        if (bytes.length > 0) {
            final String body = new String(bytes, encoding);
            try {
                return mapper.writeValueAsString(mapper.readValue(bytes, Object.class));
            } catch (Exception e) {
                return body;
            }
        }
        return null;
    }

    private Map<String, Object> buildRequestTags(final ContentCachingRequestWrapper request) {
        final Map<String, Object> tags = new LinkedHashMap<>();
        tags.put("type", "request");
        tags.put("method", request.getMethod());

        return tags;
    }

    private Map<String, Object> buildResponseTags(final ContentCachingRequestWrapper request,
                                                  final ContentCachingResponseWrapper response,
                                                  long elapsedMillis) {
        final Map<String, Object> tags = new LinkedHashMap<>();
        tags.put("type", "response");
        tags.put("method", request.getMethod());
        tags.put("status", response.getStatus());
        tags.put("elapsed_millis", elapsedMillis);

        return tags;
    }

    private String buildLogTag(final Map.Entry<String, Object> tag) {
        return String.format("[%s:%s]", tag.getKey(), tag.getValue());
    }
}
