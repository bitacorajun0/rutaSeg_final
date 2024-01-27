package com.jumani.rutaseg.controller;

import com.jumani.rutaseg.domain.User;
import com.jumani.rutaseg.dto.request.UserRequest;
import com.jumani.rutaseg.dto.response.SessionInfo;
import com.jumani.rutaseg.dto.response.UserResponse;
import com.jumani.rutaseg.dto.result.PaginatedResult;
import com.jumani.rutaseg.exception.ForbiddenException;
import com.jumani.rutaseg.exception.NotFoundException;
import com.jumani.rutaseg.exception.ValidationException;
import com.jumani.rutaseg.handler.Session;
import com.jumani.rutaseg.repository.UserRepository;
import com.jumani.rutaseg.service.PasswordService;
import com.jumani.rutaseg.util.PaginationUtil;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.Objects;
import java.util.Optional;

@RestController
@Transactional
@RequestMapping("/users")
public class UserController {

    private final UserRepository userRepo;

    private final PasswordService passwordService;

    public UserController(UserRepository userRepo,
                          PasswordService passwordService) {

        this.userRepo = userRepo;
        this.passwordService = passwordService;
    }
    @PostMapping
    public ResponseEntity<UserResponse> createUser(@RequestBody @Valid UserRequest userRequest, @Session SessionInfo session) {
        if (!session.admin()) {
            throw new ForbiddenException();
        }

        if (StringUtils.isBlank(userRequest.getPassword())) {
            throw new ValidationException("empty_password", "a password must be defined");
        }

        if (userRepo.existsByEmail(userRequest.getEmail())) {
            throw new ValidationException("user_email_exists", "user with the same email already exists");
        }

        String encryptedPassword = passwordService.encrypt(userRequest.getPassword());
        User newUser = new User(userRequest.getNickname(), encryptedPassword, userRequest.getEmail(), userRequest.isAdmin());
        User savedUser = userRepo.save(newUser);

        UserResponse userResponse = createResponse(savedUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(userResponse);
    }
    private UserResponse createResponse(User user) {
        return new UserResponse(user.getId(), user.getNickname(), user.getEmail(), user.isAdmin());
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable("id") Long id,
            @RequestBody @Valid UserRequest userRequest,
            @Session SessionInfo session) {
        // Verificar que el usuario de la sesión sea administrador
        if (!session.admin()) {
            throw new ForbiddenException();
        }

        // Buscar al usuario por ID
        User user = userRepo.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format("user with user_id [%s] not found", id)));

        // Verificar si el nuevo correo electrónico ya existe en la base de datos
        if (!user.getEmail().equals(userRequest.getEmail()) && userRepo.existsByEmail(userRequest.getEmail())) {
            throw new ValidationException("user_email_exists", "user with the same email already exists");
        }

        final String password = Optional.ofNullable(userRequest.getPassword())
                .map(passwordService::encrypt)
                .orElse(user.getPassword());

        // Actualizar los datos del usuario utilizando el método update
       user.update(
                userRequest.getNickname(),
                password,
                userRequest.getEmail(),
                userRequest.isAdmin()
        );

        // Guardar el usuario actualizado en la base de datos
        User updatedUser = userRepo.save(user);

        // Crear y devolver la respuesta con los datos actualizados del usuario
        UserResponse userResponse = createResponse(updatedUser);
        return ResponseEntity.ok(userResponse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable("id") Long id, @Session SessionInfo session) {
        User user = userRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("user not found"));

        if (!session.admin() && !Objects.equals(user.getId(), session.userId())) {
            throw new ForbiddenException();
        }
        UserResponse userResponse = createResponse(user);
        return ResponseEntity.ok(userResponse);
    }

    @GetMapping
    public ResponseEntity<PaginatedResult<UserResponse>> searchUsers(
            @RequestParam(value = "page_size", required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(value = "page", required = false, defaultValue = "1") Integer page,
            @RequestParam(value = "admin", required = false) Boolean admin,
            @RequestParam(value = "nickname", required = false) String nickname,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "with_client", required = false) Boolean withClient,
            @Session SessionInfo session) {

        if (!session.admin()) {
            throw new ForbiddenException();
        }

        final long totalElements = userRepo.count(admin, nickname, email, withClient);

        final PaginatedResult<UserResponse> result = PaginationUtil.get(totalElements, pageSize, page, (offset, limit) -> {
            List<User> users = userRepo.search(
                    admin,
                    nickname,
                    email,
                    withClient,
                    offset,
                    limit
            );

            return users.stream()
                    .map(this::createResponse)
                    .toList();
        });

        return ResponseEntity.ok(result);

    }
}



