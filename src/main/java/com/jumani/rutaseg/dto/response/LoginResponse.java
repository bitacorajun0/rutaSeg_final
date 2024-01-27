package com.jumani.rutaseg.dto.response;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;


@EqualsAndHashCode
@AllArgsConstructor
@Getter
public class LoginResponse {
    private String token;
    private UserResponse user;
}
