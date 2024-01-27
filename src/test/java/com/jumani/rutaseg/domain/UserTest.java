package com.jumani.rutaseg.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UserTest {

    @Test
    public void testUser() {
        String nickname = "John";
        String password = "password123";
        String email = "john@example.com";
        boolean admin = true;

        User user = new User(nickname, password, email, admin);

        assertNotNull(user);
        assertEquals(nickname, user.getNickname());
        assertEquals(password, user.getPassword());
        assertEquals(email, user.getEmail());
        assertEquals(admin, user.isAdmin());
    }
}