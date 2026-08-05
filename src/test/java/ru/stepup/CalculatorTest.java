package ru.stepup;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class CalculatorTest {

    private final Calculator calculator = new Calculator();

    @Test
    @Tag("fast")
    @DisplayName("Addition of two positive numbers")
    void testAdd() {
        assertEquals(5, calculator.add(2, 3));
    }

    @Test
    @DisplayName("Subtraction resulting in negative")
    void testSubtract() {
        assertEquals(-1, calculator.subtract(2, 3));
    }

    @Test
    @DisplayName("Multiplication by zero")
    void testMultiplyByZero() {
        assertEquals(0, calculator.multiply(5, 0));
    }

    @Test
    @DisplayName("Division of two integers")
    void testDivide() {
        assertEquals(2, calculator.divide(6, 3));
    }

    @Test
    @DisplayName("Division by zero throws exception")
    void testDivideByZero() {
        assertThrows(IllegalArgumentException.class, () -> calculator.divide(1, 0));
    }

    @ParameterizedTest
    @CsvSource({"1, 2, 3", "10, 20, 30", "0, 0, 0", "-1, 1, 0"})
    @DisplayName("Parameterized addition test")
    void testAddParameterized(int a, int b, int expected) {
        assertEquals(expected, calculator.add(a, b));
    }
}
