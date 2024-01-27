package com.jumani.rutaseg.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class UserRequest {

    @NotEmpty
    private String nickname;
    @Email
    @NotEmpty
    private String email;
    private String password;
    private boolean admin;
}
