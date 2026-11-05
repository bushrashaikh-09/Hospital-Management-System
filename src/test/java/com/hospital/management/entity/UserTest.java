package com.hospital.management.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

class UserTest {

    @Test
    void prePersist_shouldNormalizeUserValues() {
        User user = new User();
        user.setFullName("  Alice Johnson  ");
        user.setEmail(" USER@Example.com ");
        user.setPassword("  pass123  ");
        user.setRole("user");

        user.prePersist();

        assertEquals("Alice Johnson", user.getFullName());
        assertEquals("user@example.com", user.getEmail());
        assertEquals("pass123", user.getPassword());
        assertEquals("USER", user.getRole());
        assertEquals(Boolean.TRUE, user.isEnabled());
    }
}
