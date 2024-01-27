package com.jumani.rutaseg.dto.request;

import com.jumani.rutaseg.domain.CostType;
import lombok.Getter;
import lombok.Setter;
import lombok.NonNull;

@Getter
@Setter
public class CostRequest {

    @NonNull
    private double amount;

    @NonNull
    private CostType type;

    private String description;
}