package com.jumani.rutaseg.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;

public class ConsigneeRequest {
    @NotEmpty(message = "name cannot be empty")
    private String name;

    @Positive(message = "CUIT must be a positive number")
    private long cuit;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getCuit() {
        return cuit;
    }

    public void setCuit(long cuit) {
        this.cuit = cuit;
    }
}