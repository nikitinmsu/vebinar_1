package ru.stepup;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * =====================================================================
 *  AssertJ — объекты, Optional, Boolean и «мягкие» проверки
 * =====================================================================
 *
 *  Здесь собраны проверки, которые не относятся к одному конкретному типу:
 *  равенство объектов, типы, поля, Optional, булевы значения,
 *  а также SoftAssertions — механизм сбора нескольких ошибок за один прогон.
 *
 *  Запустить только этот класс:
 *     ./gradlew test --tests "ru.stepup.AssertJ_ObjectAssertionsTest"
 */
class AssertJ_ObjectAssertionsTest {

    @Test
    @DisplayName("Равенство по содержимому vs по ссылке")
    void equalityVsIdentity() {
        // Два РАЗНЫХ объекта, но с одинаковым содержимым.
        String a = new String("abc");
        String b = new String("abc");

        // isEqualTo — сравнение через equals(): содержимое совпадает -> ОК.
        assertThat(a).isEqualTo(b);

        // isSameAs — сравнение по ССЫЛКЕ: разные объекты -> НЕ одно и то же.
        assertThat(a).isNotSameAs(b);

        // А вот одна и та же ссылка — это «то же самое».
        assertThat(a).isSameAs(a);

        // Вывод: isEqualTo сравнивает содержимое, isSameAs — ссылку.
    }

    @Test
    @DisplayName("Null-проверки")
    void nullChecks() {
        String nothing = null;
        assertThat(nothing).isNull();

        String value = "x";
        assertThat(value).isNotNull();
    }

    @Test
    @DisplayName("Проверка типов")
    void types() {
        Object value = "some string";

        assertThat(value).isInstanceOf(String.class);
        // String — наследник CharSequence, поэтому эта проверка тоже проходит.
        assertThat(value).isInstanceOf(CharSequence.class);
        assertThat(value).isNotInstanceOf(Integer.class);
        // Точный класс: именно String, а не любой наследник.
        assertThat(value).isExactlyInstanceOf(String.class);
    }

    @Test
    @DisplayName("Проверка полей объекта")
    void fields() {
        User user = new User("Alice", "alice@example.com", 25);

        // Проверка через геттер/поле объекта по имени.
        assertThat(user).hasFieldOrPropertyWithValue("name", "Alice");
        assertThat(user).hasFieldOrPropertyWithValue("email", "alice@example.com");
        assertThat(user).hasFieldOrPropertyWithValue("age", 25);
    }

    @Test
    @DisplayName("Рекурсивное сравнение объектов")
    void recursiveComparison() {
        User user1 = new User("Alice", "alice@example.com", 25);
        User user2 = new User("Alice", "alice@example.com", 25);

        // У класса User переопределён equals(), поэтому isEqualTo достаточно.
        assertThat(user1).isEqualTo(user2);

        // usingRecursiveComparison сравнивает ПОЛЕ ЗА ПОЛЕМ, игнорируя equals().
        // Нужно, когда equals() не переопределён или сравнивает не все поля.
        assertThat(user1).usingRecursiveComparison().isEqualTo(user2);

        // Из сравнения можно исключить отдельные поля.
        User userWithDifferentAge = new User("Alice", "alice@example.com", 99);
        assertThat(user1)
                .usingRecursiveComparison()
                .ignoringFields("age")
                .isEqualTo(userWithDifferentAge);
    }

    @Test
    @DisplayName("Optional")
    void optionals() {
        Optional<String> present = Optional.of("value");

        assertThat(present).isPresent();               // значение внутри есть
        assertThat(present).contains("value");         // внутри ровно "value"
        assertThat(present).get().isEqualTo("value");  // достали и проверили
        assertThat(present).hasValueSatisfying(v ->
                assertThat(v).startsWith("val"));      // значение подходит под проверку

        Optional<String> empty = Optional.empty();
        assertThat(empty).isEmpty();
    }

    @Test
    @DisplayName("Boolean")
    void booleans() {
        User adult = new User("Alice", "a@example.com", 30);
        User child = new User("Bob", "b@example.com", 10);

        assertThat(adult.isAdult()).isTrue();
        assertThat(child.isAdult()).isFalse();
    }

    @Test
    @DisplayName("Принадлежность множеству")
    void membership() {
        assertThat("a").isIn("a", "b", "c");
        assertThat("a").isNotIn("x", "y");
    }

    @Test
    @DisplayName("Мягкие проверки: SoftAssertions")
    void softAssertions() {
        // Обычный assertThat падает на ПЕРВОЙ же ошибке.
        // assertSoftly собирает ВСЕ проверки и в конце сообщает обо всех провалах
        // сразу. Незаменимо, когда за один прогон нужно увидеть все проблемы.
        assertSoftly(softly -> {
            softly.assertThat(1 + 1).isEqualTo(2);
            softly.assertThat("abc").hasSize(3);
            softly.assertThat(List.of(1, 2)).contains(2);
        });

        // Если бы одна из проверок не прошла, тест упал бы в КОНЦЕ,
        // и в сообщении об ошибке были бы перечислены ВСЕ провалы сразу,
        // а не только первый. Это ключевое отличие от обычного assertThat.
    }
}
