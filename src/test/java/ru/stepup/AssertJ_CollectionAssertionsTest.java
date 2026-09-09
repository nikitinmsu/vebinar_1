package ru.stepup;

import org.assertj.core.api.Condition;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.assertj.core.api.Assertions.tuple;

/**
 * =====================================================================
 *  AssertJ — проверки для КОЛЛЕКЦИЙ (списки, массивы, Map)
 * =====================================================================
 *
 *  У AssertJ для коллекций много «умных» проверок: точный состав, порядок,
 *  подпоследовательности, извлечение полей, условия по предикатам.
 *
 *  Запустить только этот класс:
 *     ./gradlew test --tests "ru.stepup.AssertJ_CollectionAssertionsTest"
 */
class AssertJ_CollectionAssertionsTest {

    /** Список, с которым работает большинство примеров. */
    private final List<String> names = List.of("Alice", "Bob", "Charlie");

    @Test
    @DisplayName("Размер и «пустота»")
    void sizeAndEmptiness() {
        assertThat(names).hasSize(3);
        assertThat(names).hasSizeBetween(2, 5);       // размер в диапазоне [2; 5]
        assertThat(names).isNotEmpty();
        assertThat(List.<String>of()).isEmpty();
    }

    @Test
    @DisplayName("Проверки вхождения")
    void contains() {
        assertThat(names).contains("Alice");             // элемент есть
        assertThat(names).contains("Alice", "Bob");      // ВСЕ перечисленные есть
        assertThat(names).containsAnyOf("Zack", "Bob");  // хотя бы ОДИН из перечисленных
        assertThat(names).doesNotContain("Zack");        // элемента нет
        assertThat(names).containsOnlyOnce("Bob");       // элемент встречается ровно 1 раз

        // Состав совпадает, порядок не важен.
        assertThat(names).containsOnly("Bob", "Alice", "Charlie");

        // Наша коллекция является подмножеством заданной.
        assertThat(names).isSubsetOf(List.of("Alice", "Bob", "Charlie", "David"));
    }

    @Test
    @DisplayName("Проверки с учётом порядка")
    void order() {
        // Строгий состав И порядок: только эти элементы, именно в этом порядке.
        assertThat(names).containsExactly("Alice", "Bob", "Charlie");

        // Те же элементы, порядок НЕ важен.
        assertThat(names).containsExactlyInAnyOrder("Charlie", "Bob", "Alice");

        // Первые и последние элементы.
        assertThat(names).startsWith("Alice");
        assertThat(names).endsWith("Charlie");

        // Подпоследовательность: "Alice" встречается раньше "Charlie".
        assertThat(names).containsSubsequence("Alice", "Charlie");
    }

    @Test
    @DisplayName("Проверки через предикаты (условия)")
    void predicates() {
        assertThat(names).allMatch(n -> n.length() > 2);    // ВСЕ элементы под условие
        assertThat(names).anyMatch(n -> n.startsWith("C")); // хотя бы ОДИН подходит
        assertThat(names).noneMatch(n -> n.equals("Zack")); // НИ ОДИН не подходит

        // allSatisfy — над КАЖДЫМ элементом выполняется несколько проверок.
        // Если хотя бы для одного элемента хоть одна проверка упадёт — тест красный.
        assertThat(names).allSatisfy(name -> {
            assertThat(name).isNotBlank();
            assertThat(name.length()).isLessThan(10);
        });
    }

    @Test
    @DisplayName("Доступ к отдельным элементам")
    void elements() {
        assertThat(names).first().isEqualTo("Alice");   // первый элемент
        assertThat(names).last().isEqualTo("Charlie");  // последний элемент
        assertThat(names).element(1).isEqualTo("Bob");  // элемент по индексу
    }

    @Test
    @DisplayName("Извлечение свойств: extracting(...)")
    void extracting() {
        // extracting превращает список в список «извлечённых» значений.
        assertThat(names).extracting(String::length)
                .containsExactly(5, 3, 7);   // Alice=5, Bob=3, Charlie=7

        // Работает и с объектами проекта: у каждого User берём getName().
        List<User> users = List.of(
                new User("Alice", "alice@example.com", 25),
                new User("Bob", "bob@example.com", 30)
        );
        assertThat(users).extracting(User::getName)
                .containsExactly("Alice", "Bob");

        // Несколько полей сразу — результат «кортежи» (tuple):
        // получится список (Alice, 25) и (Bob, 30), которые сравниваем через tuple().
        assertThat(users).extracting(User::getName, User::getAge)
                .containsExactly(
                        tuple("Alice", 25),
                        tuple("Bob", 30)
                );
    }

    @Test
    @DisplayName("Переиспользуемые условия: Condition")
    void conditions() {
        // Condition — это «проверка», которую можно сохранить и применить
        // сразу к нескольким элементам. Второй аргумент — описание для отчёта.
        Condition<String> noDigits = new Condition<>(
                s -> s.chars().noneMatch(Character::isDigit),
                "не содержит цифр");
        Condition<String> threeLetters = new Condition<>(
                s -> s.length() == 3,
                "ровно из 3 букв");

        assertThat(names).are(noDigits);                 // КАЖДЫЙ элемент: без цифр
        assertThat(names).have(noDigits);                // синоним are(...)
        assertThat(names).haveExactly(1, threeLetters);  // ровно 1 элемент — Bob
        assertThat(names).areAtMost(1, threeLetters);    // не больше 1 такого элемента
    }

    @Test
    @DisplayName("Массивы")
    void arrays() {
        int[] arr = {1, 2, 3, 4};

        assertThat(arr).hasSize(4);
        assertThat(arr).contains(3, 1);               // все перечисленные есть
        assertThat(arr).doesNotContain(9);
        assertThat(arr).containsExactly(1, 2, 3, 4);  // строгий состав и порядок
        assertThat(arr).startsWith(1, 2);
        assertThat(arr).endsWith(3, 4);
    }

    @Test
    @DisplayName("Map")
    void maps() {
        Map<String, Integer> ages = Map.of("Alice", 25, "Bob", 30);

        assertThat(ages).containsKey("Alice");
        assertThat(ages).containsKeys("Alice", "Bob");
        assertThat(ages).doesNotContainKey("Zack");
        assertThat(ages).containsValue(25);
        assertThat(ages).containsEntry("Alice", 25);
        assertThat(ages).doesNotContainEntry("Bob", 25);  // у Bob возраст 30, не 25
        assertThat(ages).hasSize(2);

        // Точный состав: только эти пары ключ=значение (порядок не важен).
        assertThat(ages).containsExactly(entry("Alice", 25), entry("Bob", 30));
    }
}
