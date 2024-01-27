package com.jumani.rutaseg.dto.response;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode
@AllArgsConstructor
@Getter
public class UserResponse {
    private Long id;
    private String nickname;
    private String email;
    private boolean admin;
}