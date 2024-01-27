package com.jumani.rutaseg.handler;

import com.jumani.rutaseg.dto.response.SessionInfo;
import com.jumani.rutaseg.exception.UnauthorizedException;
import com.jumani.rutaseg.service.auth.JwtService;
import com.jumani.rutaseg.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
@AllArgsConstructor
public class SessionInfoHandler implements HandlerMethodArgumentResolver {

    private final JwtService jwtService;

    @Override
    public boolean supportsParameter(MethodParameter methodParameter) {
        return methodParameter.getParameterAnnotation(Session.class) != null;
    }

    @Override
    public SessionInfo resolveArgument(@NonNull MethodParameter methodParameter,
                                       ModelAndViewContainer modelAndViewContainer,
                                       NativeWebRequest nativeWebRequest,
                                       WebDataBinderFactory webDataBinderFactory) {

        final HttpServletRequest request = (HttpServletRequest) nativeWebRequest.getNativeRequest();

        final String token = JwtUtil.extractToken(request).orElseThrow(UnauthorizedException::new);

        final long userId = Long.parseLong(jwtService.extractSubject(token));
        final boolean admin = jwtService.isAdminToken(token);

        return new SessionInfo(userId, admin);
    }
}
