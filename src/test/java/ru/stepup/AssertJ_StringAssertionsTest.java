package ru.stepup;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * =====================================================================
 *  AssertJ — проверки для СТРОК (StringAssert)
 * =====================================================================
 *
 *  Как это работает:
 *    1. assertThat(value) создаёт объект-«ассертер» (здесь StringAssert),
 *       который «держит» проверяемое значение.
 *    2. Каждый метод проверки возвращает САМОГО ЖЕ ассертера (this) —
 *       поэтому методы можно писать ЦЕПОЧКОЙ, без промежуточных переменных.
 *       Это называется fluent API («текучий» интерфейс).
 *    3. Если проверка не проходит — AssertJ бросает AssertionError
 *       с человекочитаемым сообщением, и тест становится «красным».
 *
 *  Чем отличается от JUnit-ассертов:
 *     JUnit:   assertEquals(expected, actual)   — легко перепутать аргументы
 *     AssertJ: assertThat(actual).isEqualTo(...) — «сначала то, что проверяем»
 *
 *  Запустить только этот класс:
 *     ./gradlew test --tests "ru.stepup.AssertJ_StringAssertionsTest"
 */
class AssertJ_StringAssertionsTest {

    /** Строка, над которой работает большинство примеров ниже. */
    private static final String TEXT = "Hello, World!";

    @Test
    @DisplayName("Равенство и отличие")
    void equality() {
        // isEqualTo — то же самое, что equals(...): проверка точного содержимого.
        assertThat(TEXT).isEqualTo("Hello, World!");

        // Негативная проверка: содержимое НЕ равно заданной строке.
        assertThat(TEXT).isNotEqualTo("hello, world!");

        // Сравнение БЕЗ учёта регистра: H == h, W == w и т.д.
        assertThat(TEXT).isEqualToIgnoringCase("hello, world!");

        // Сравнение, при котором игнорируются ВСЕ пробельные символы
        // в обеих строках (не только по краям!). "h  e l l o" == "hello".
        assertThat("h  e l l o").isEqualToIgnoringWhitespace("hello");
    }

    @Test
    @DisplayName("Вхождение подстрок")
    void contains() {
        // Строка СОДЕРЖИТ подстроку "World".
        assertThat(TEXT).contains("World");

        // Строка НЕ содержит "Mars".
        assertThat(TEXT).doesNotContain("Mars");

        // Можно передать сразу несколько подстрок — ДОЛЖНЫ встретиться все.
        assertThat(TEXT).contains("Hello", "World");

        // Вхождение без учёта регистра.
        assertThat(TEXT).containsIgnoringCase("wORLD");

        // Ровно ОДНО вхождение подстроки. ("World" встречается в тексте один раз.)
        assertThat(TEXT).containsOnlyOnce("World");

        // containsSequence — подстроки должны идти ПОДРЯД, друг за другом.
        assertThat(TEXT).containsSequence("Hello,", " World!");

        // containsSubsequence — порядок соблюдён, но между подстроками
        // может быть что угодно ("Hello" ... ", " ... "World").
        assertThat(TEXT).containsSubsequence("Hello", "World");
    }

    @Test
    @DisplayName("Начало и конец строки")
    void startEnd() {
        assertThat(TEXT).startsWith("Hello");
        assertThat(TEXT).endsWith("World!");

        // Те же проверки, но без учёта регистра.
        assertThat(TEXT).startsWithIgnoringCase("hello");
        assertThat(TEXT).endsWithIgnoringCase("WORLD!");
    }

    @Test
    @DisplayName("Регулярные выражения")
    void regex() {
        // matches — ВСЯ строка целиком должна соответствовать регулярному выражению.
        assertThat(TEXT).matches("Hello, W.*");
        assertThat(TEXT).matches("[A-Z][a-z]+, [A-Z][a-z]+!");

        // doesNotMatch — строка НЕ должна соответствовать выражению.
        assertThat(TEXT).doesNotMatch("Goodbye.*");
    }

    @Test
    @DisplayName("Длина строки")
    void length() {
        // hasSize — число символов в строке.
        assertThat(TEXT).hasSize(13);
    }

    @Test
    @DisplayName("Пустота, пробелы и регистр")
    void blankAndCase() {
        assertThat("").isEmpty();          // пустая строка "" — 0 символов
        assertThat("  ").isBlank();        // только пробельные символы
        assertThat(TEXT).isNotBlank();     // в строке есть непробельные символы
        assertThat("hello").isLowerCase(); // все буквы строчные
        assertThat("HELLO").isUpperCase(); // все буквы прописные
    }

    @Test
    @DisplayName("Подстрока другой строки и принадлежность множеству")
    void misc() {
        // "World" является подстрокой "Hello, World!".
        assertThat("World").isSubstringOf("Hello, World!");

        // Значение входит в указанное множество.
        assertThat("a").isIn("a", "b", "c");

        // Значение НЕ входит в указанное множество.
        assertThat("z").isNotIn("a", "b", "c");
    }

    @Test
    @DisplayName("Собственное описание проверки: as(...)")
    void customDescription() {
        // as(...) задаёт «подпись» проверки. Она появится в сообщении об ошибке
        // и поможет понять, ЧТО именно мы проверяли и ГДЕ упало.
        // Удобно, когда одно значение проверяется в разных контекстах.
        assertThat(TEXT)
                .as("Строка %s должна начинаться с приветствия", TEXT)
                .startsWith("Небо");

        // Как выглядела бы ошибка БЕЗ as(...):
        //   java.lang.AssertionError:
        //   expected: "Hello, World!" to start with "Goodbye"
        //
        // И С as(...) — заголовок становится человекочитаемым:
        //   java.lang.AssertionError: [Строка Hello, World! должна начинаться
        //   с приветствия] expected: "Hello, World!" to start with "Goodbye"
    }
}
