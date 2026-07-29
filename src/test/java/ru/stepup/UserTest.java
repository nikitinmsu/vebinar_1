package ru.stepup;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    private User adultUser;
    private User childUser;

    @BeforeEach
    void setUp() {
        adultUser = new User("Alice", "alice@example.com", 25);
        childUser = new User("Bob", "bob@example.com", 12);
    }

    @Test
    @DisplayName("User is created with correct name")
    void testGetName() {
        assertEquals("Alice", adultUser.getName());
    }

    @Test
    @DisplayName("User is created with correct email")
    void testGetEmail() {
        assertEquals("alice@example.com", adultUser.getEmail());
    }

    @Test
    @DisplayName("Adult user is recognized as adult")
    void testIsAdultForAdult() {
        assertTrue(adultUser.isAdult());
    }

    @Test
    @DisplayName("Child user is recognized as not adult")
    void testIsAdultForChild() {
        assertFalse(childUser.isAdult());
    }

    @Test
    @DisplayName("Birthday increments age")
    void testHaveBirthday() {
        int oldAge = childUser.getAge();
        childUser.haveBirthday();
        assertEquals(oldAge + 1, childUser.getAge());
    }

    @Test
    @DisplayName("Greeting message is correct")
    void testGetGreeting() {
        assertEquals("Hello, my name is Alice", adultUser.getGreeting());
    }

    @Test
    @DisplayName("Users with same fields are equal")
    void testEquals() {
        User sameUser = new User("Alice", "alice@example.com", 25);
        assertEquals(adultUser, sameUser);
        assertEquals(adultUser.hashCode(), sameUser.hashCode());
    }

    @Test
    @DisplayName("Users with different fields are not equal")
    void testNotEquals() {
        assertNotEquals(adultUser, childUser);
    }
}
