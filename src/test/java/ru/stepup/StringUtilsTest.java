package ru.stepup;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StringUtilsTest {

    @Test
    @DisplayName("Reverse a normal string")
    void testReverse() {
        assertEquals("dcba", StringUtils.reverse("abcd"));
    }

    @Test
    @DisplayName("Reverse null returns null")
    void testReverseNull() {
        assertNull(StringUtils.reverse(null));
    }

    @Test
    @DisplayName("Reverse empty string")
    void testReverseEmpty() {
        assertEquals("", StringUtils.reverse(""));
    }

    @Test
    @DisplayName("Palindrome is detected correctly")
    void testIsPalindrome() {
        assertTrue(StringUtils.isPalindrome("A man a plan a canal Panama"));
    }

    @Test
    @DisplayName("Non-palindrome returns false")
    void testIsNotPalindrome() {
        assertFalse(StringUtils.isPalindrome("hello"));
    }

    @Test
    @DisplayName("Null is not a palindrome")
    void testIsPalindromeNull() {
        assertFalse(StringUtils.isPalindrome(null));
    }

    @Test
    @DisplayName("Count vowels in a string")
    void testCountVowels() {
        assertEquals(3, StringUtils.countVowels("Hello World"));
    }

    @Test
    @DisplayName("Count vowels in null returns zero")
    void testCountVowelsNull() {
        assertEquals(0, StringUtils.countVowels(null));
    }

    @Test
    @DisplayName("Convert camelCase to snake_case")
    void testToSnakeCase() {
        assertEquals("hello_world", StringUtils.toSnakeCase("helloWorld"));
    }
}
