package com.jumani.rutaseg.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter

public class ConsigneeDataRequest {

    @NotEmpty
    private String name;

    @NotNull
    @Positive
    private Long cuit;

}
