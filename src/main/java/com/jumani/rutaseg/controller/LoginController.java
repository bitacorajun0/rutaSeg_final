package com.jumani.rutaseg.controller;

import com.jumani.rutaseg.dto.request.LoginRequest;
import com.jumani.rutaseg.dto.response.LoginResponse;
import com.jumani.rutaseg.dto.response.UserResponse;
import com.jumani.rutaseg.exception.ValidationException;
import com.jumani.rutaseg.repository.UserRepository;
import com.jumani.rutaseg.service.PasswordService;
import com.jumani.rutaseg.service.auth.JwtService;
import com.jumani.rutaseg.util.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoginController {

    private final UserRepository userRepository;
    private final PasswordService passwordService;
    private final JwtService jwtService;

    public LoginController(UserRepository userRepository,
                           PasswordService passwordService,
                           JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordService = passwordService;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest loginRequest,
                                               HttpServletResponse response) {
        String email = loginRequest.getEmail();
        String password = loginRequest.getPassword();

        return userRepository.findOneByEmail(email)
                .filter(user -> passwordService.matches(password, user.getPassword()))
                .map(user -> {
                    final String token = jwtService.generateToken(user.getId().toString(), user.isAdmin());

                    final Cookie cookie = new Cookie(JwtUtil.JWT_TOKEN_COOKIE_NAME, token);
                    cookie.setHttpOnly(true);
                    cookie.setMaxAge(7200); // 2 hours in seconds
                    cookie.setPath("/");
                    cookie.setDomain(null);

                    response.addCookie(cookie);

                    final UserResponse userResponse = new UserResponse(user.getId(), user.getNickname(), user.getEmail(), user.isAdmin());
                    return new LoginResponse(token, userResponse);

                })
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ValidationException("invalid_credentials", "invalid email or password"));
    }
}
