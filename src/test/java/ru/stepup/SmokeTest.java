package ru.stepup;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Smoke-тесты: максимально быстрая проверка «жива ли» базовая логика.
 *
 * Отбираются задачей smokeTest по тегу @Tag("smoke"):
 *   ./gradlew smokeTest
 *
 * Идея: если падает smoke-тест — вся система, скорее всего, «лежит»,
 * и гонять остальные (особенно долгие) тесты уже нет смысла. Поэтому
 * в задаче стоит failFast = true и жёсткий таймаут.
 *
 * Класс помечен ДВУМЯ тегами — @Tag("smoke") и @Tag("fast").
 * Это позволяет показать пересечение тегов: выражение "smoke & fast"
 * отберёт только такие классы, где есть ОБА тега одновременно.
 */
@Tag("smoke")
@Tag("fast")
class SmokeTest {

    @Test
    @DisplayName("Calculator basic sanity")
    void testCalculator() {
        assertEquals(3, new Calculator().add(1, 2));
    }

    @Test
    @DisplayName("StringUtils basic sanity")
    void testStringUtils() {
        assertEquals("olleh", StringUtils.reverse("hello"));
    }

    @Test
    @DisplayName("User basic sanity")
    void testUser() {
        assertTrue(new User("A", "a@b.c", 30).isAdult());
    }
}
