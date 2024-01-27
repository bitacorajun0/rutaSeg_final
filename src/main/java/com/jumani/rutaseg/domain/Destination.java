package com.jumani.rutaseg.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;

@Getter
@Embeddable
public class Destination {

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private DestinationType type;

    @Column(name = "code")
    private String code;

    @Column(name = "fob")
    private String fob;

    @Column(name = "currency")
    private String currency;

    @Column(name = "product_details", columnDefinition = "text")
    private String productDetails;

    private Destination() {
    }

    public Destination(DestinationType type,
                       String code,
                       String fob,
                       String currency,
                       String productDetails) {

        this.type = type;
        this.code = code;
        this.fob = fob;
        this.currency = currency;
        this.productDetails = productDetails;
    }
}
