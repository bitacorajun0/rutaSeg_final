package com.jumani.rutaseg.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class OrderReportRequest {

    private Long clientId;
    @NotNull
    private LocalDate dateFrom;

    @NotNull
    private LocalDate dateTo;
}
