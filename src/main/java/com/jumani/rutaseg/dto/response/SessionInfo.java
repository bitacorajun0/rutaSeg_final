package com.jumani.rutaseg.dto.response;

import com.jumani.rutaseg.domain.UserType;

public record SessionInfo(long userId, boolean admin) {
    public UserType getUserType() {
        return admin ? UserType.ADMIN : UserType.CLIENT;
    }
}
