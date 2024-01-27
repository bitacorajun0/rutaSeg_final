package com.jumani.rutaseg.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class ClientRequest {
    @NotEmpty
    private String name;
    private String phone;
    @Positive
    private Long cuit;
    @Positive
    private Long userId;
}
