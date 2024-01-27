package com.jumani.rutaseg.service;

import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

@Service
public class PasswordService {

    public final String encrypt(String raw) {
        return BCrypt.hashpw(raw, BCrypt.gensalt());
    }

    public final boolean matches(String raw, String encrypted) {
        return BCrypt.checkpw(raw, encrypted);
    }
}
