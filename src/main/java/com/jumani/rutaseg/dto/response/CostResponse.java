package com.jumani.rutaseg.dto.response;

import com.jumani.rutaseg.domain.CostType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import java.time.ZonedDateTime;

@AllArgsConstructor
@Getter
public class CostResponse {
    private Long id;
    private double amount;
    private String description;
    private CostType type;
    private ZonedDateTime createdAt;
}
