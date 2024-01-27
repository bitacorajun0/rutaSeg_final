package com.jumani.rutaseg.controller;
import com.jumani.rutaseg.domain.User;
import com.jumani.rutaseg.dto.request.UserRequest;
import com.jumani.rutaseg.dto.response.SessionInfo;
import com.jumani.rutaseg.dto.response.UserResponse;
import com.jumani.rutaseg.exception.ForbiddenException;
import com.jumani.rutaseg.exception.ValidationException;
import com.jumani.rutaseg.repository.UserRepository;
import com.jumani.rutaseg.service.PasswordService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)

class UserControllerTest {

    @Mock
    UserRepository userRepo;

    @Mock
    private PasswordService passwordService;

    @InjectMocks
    private UserController userController;

    @Test
    void create_NotAdminSession_Forbidden() {
        // Arrange
        final SessionInfo session = new SessionInfo(1L, false);
        final UserRequest request = new UserRequest();

        // Act & Assert
        ForbiddenException exception = assertThrows(ForbiddenException.class,
                () -> userController.createUser(request, session));

        verifyNoInteractions(userRepo, passwordService);
    }

    @Test
    void createUser_EmailExists_ValidationException() {
        // Arrange
        final SessionInfo session = new SessionInfo(1L, true);
        final UserRequest request = new UserRequest("John Doe", "johndoe@example.com", "password", true);

        when(userRepo.existsByEmail(request.getEmail())).thenReturn(true);

        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class,
                () -> userController.createUser(request, session));

        assertEquals("user_email_exists", exception.getCode());
        assertEquals("user with the same email already exists", exception.getMessage());

        verify(userRepo).existsByEmail(request.getEmail());
        verifyNoMoreInteractions(userRepo, passwordService);
    }

    @Test
    void createUser_UserCreateOk() {
        // Arrange
        final SessionInfo session = new SessionInfo(1L, true);
        final UserRequest request = new UserRequest("John Doe", "johndoe@example.com", "password", true);

        when(userRepo.existsByEmail(request.getEmail())).thenReturn(false);

        final String encryptedPassword = "encryptedPassword";
        final User newUser = new User(request.getNickname(), encryptedPassword, request.getEmail(), request.isAdmin());
        final User savedUser = new User( request.getNickname(), request.getEmail(),request.getPassword(), request.isAdmin());

        doReturn(encryptedPassword).when(passwordService).encrypt(request.getPassword());
        doReturn(savedUser).when(userRepo).save(any(User.class));

        // Act
        ResponseEntity<UserResponse> response = userController.createUser(request, session);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());

        UserResponse expectedUserResponse = new UserResponse(savedUser.getId(), savedUser.getNickname(), savedUser.getEmail(), savedUser.isAdmin());
        assertEquals(expectedUserResponse, response.getBody());

        verify(userRepo).existsByEmail(request.getEmail());
        verify(passwordService).encrypt(request.getPassword());
        verify(userRepo).save(any(User.class));
        verifyNoMoreInteractions(userRepo, passwordService);
    }
}