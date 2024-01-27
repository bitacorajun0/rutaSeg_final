package com.jumani.rutaseg.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;

@Getter
@Embeddable
public class Consignee {

    @Column(name = "name")
    private String name;

    @Column(name = "cuit")
    private long cuit;

    public Consignee(String name, long cuit) {
        this.name = name;
        this.cuit = cuit;
    }

    public Consignee() {
    }
}