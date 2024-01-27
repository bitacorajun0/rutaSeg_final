package com.jumani.rutaseg.domain;

import lombok.Getter;

@Getter
public enum OrderStatus {
    DRAFT("BORRADOR"), REVISION("REVISION"), PROCESSING("PROCESANDO"), FINISHED("FINALIZADO"), CANCELLED("CANCELADO");


    private final String translation;

    OrderStatus(String translation) {
        this.translation = translation;
    }
}
