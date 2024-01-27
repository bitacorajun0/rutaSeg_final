package com.jumani.rutaseg.controller;

import com.jumani.rutaseg.domain.User;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginControllerTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordService passwordService;

    @Mock
    private HttpServletResponse res;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private LoginController loginController;

    @Test
    void login_Successful() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("example@example.com", "password");
        User mockUser = mock(User.class);
        String expectedToken = "generated_token";
        UserResponse expectedUserResponse = new UserResponse(1L, "John Doe", "example@example.com", true);

        when(mockUser.getId()).thenReturn(1L);
        when(mockUser.getNickname()).thenReturn("John Doe");
        when(mockUser.getEmail()).thenReturn("example@example.com");
        when(mockUser.isAdmin()).thenReturn(true);

        when(userRepository.findOneByEmail(loginRequest.getEmail())).thenReturn(Optional.of(mockUser));
        when(passwordService.matches(loginRequest.getPassword(), null)).thenReturn(true);
        when(jwtService.generateToken(anyString(), anyBoolean())).thenReturn(expectedToken);

        final Cookie expectedCookie = new Cookie(JwtUtil.JWT_TOKEN_COOKIE_NAME, expectedToken);
        expectedCookie.setHttpOnly(true);
        expectedCookie.setMaxAge(7200); // 2 hours in seconds
        expectedCookie.setPath("/");
        expectedCookie.setDomain(null);

        // Act
        ResponseEntity<LoginResponse> response = loginController.login(loginRequest, this.res);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedToken, response.getBody().getToken());

        UserResponse actualUserResponse = response.getBody().getUser();
        assertEquals(expectedUserResponse, actualUserResponse);

        // Verify method invocations
        verify(userRepository).findOneByEmail(loginRequest.getEmail());
        verify(passwordService).matches(loginRequest.getPassword(), null);
        verify(jwtService).generateToken(anyString(), anyBoolean());
        verify(res).addCookie(expectedCookie);
        verifyNoMoreInteractions(userRepository, passwordService, jwtService);
    }


    @Test
    void login_InvalidCredentials() {
        LoginRequest loginRequest = new LoginRequest("example@example.com", "password");

        when(userRepository.findOneByEmail(loginRequest.getEmail())).thenReturn(Optional.empty());

        ValidationException exception = assertThrows(ValidationException.class,
                () -> loginController.login(loginRequest, this.res));

        assertEquals("invalid_credentials", exception.getCode());
        assertTrue(exception.getMessage().equalsIgnoreCase("Invalid email or password"));

        verify(userRepository).findOneByEmail(loginRequest.getEmail());
        verifyNoMoreInteractions(userRepository, passwordService, jwtService);
        verifyNoInteractions(res);
    }

    @Test
    void login_PasswordValidationFailed() {
        // Arrange
        LoginRequest loginRequest = new LoginRequest("example@example.com", "password");

        when(userRepository.findOneByEmail(loginRequest.getEmail())).thenReturn(Optional.of(mock(User.class)));
        when(passwordService.matches(loginRequest.getPassword(), null)).thenReturn(false);


        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class,
                () -> loginController.login(loginRequest, this.res));

        // Assert exception details
        assertEquals("invalid_credentials", exception.getCode());
        assertEquals("invalid email or password", exception.getMessage());

        // Verify method invocations
        verify(userRepository).findOneByEmail(loginRequest.getEmail());
        verify(passwordService).matches(loginRequest.getPassword(), null);
        verifyNoMoreInteractions(userRepository, passwordService, jwtService);
        verifyNoInteractions(res);
    }
}
