package com.jumani.rutaseg.service;

import com.jumani.rutaseg.TestDataGen;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordServiceTest {
    private final PasswordService service = new PasswordService();

    @Test
    void encrypt() {
        final String raw = TestDataGen.randomShortString();

        final String encrypt = service.encrypt(raw);

        assertNotEquals(raw, encrypt);
    }

    @Test
    void matches_True() {
        final String raw = TestDataGen.randomShortString();

        final String encrypted = service.encrypt(raw);

        assertTrue(service.matches(raw, encrypted));
    }

    @Test
    void matches_False() {
        final String raw = TestDataGen.randomShortString();

        final String encrypted = service.encrypt(raw);

        assertFalse(service.matches(raw + "a", encrypted));
    }
}
