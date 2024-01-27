package com.jumani.rutaseg.domain;

import lombok.Getter;

@Getter
public enum UserType {
    ADMIN("administrador"), CLIENT("cliente");

    private final String translation;

    UserType(String translation) {
        this.translation = translation;
    }
}
