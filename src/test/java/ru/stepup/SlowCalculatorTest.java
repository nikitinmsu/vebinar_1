package ru.stepup;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Пример «тяжёлого / медленного» теста (тег @Tag("slow")).
 *
 * Запускается задачей slowTest, которая выделяет больше памяти:
 *   ./gradlew slowTest   (в конфигурации задачи стоит jvmArgs("-Xmx2g"))
 *
 * Имитирует ресурсоёмкие проверки: большие объёмы данных, длинные циклы.
 * Такие тесты обычно выносят из обычного прогона, чтобы юнит-тесты
 * оставались быстрыми.
 */
@Tag("slow")
class SlowCalculatorTest {

    @Test
    @DisplayName("Sorting a large dataset")
    void testLargeDataset() {
        List<Integer> data = new ArrayList<>();
        for (int i = 0; i < 2_000_000; i++) {
            data.add((i * 31) % 1_000_000);
        }

        data.sort(Integer::compareTo);

        for (int i = 1; i < data.size(); i++) {
            assertTrue(data.get(i - 1) <= data.get(i), "list must be sorted");
        }
    }

    @Test
    @DisplayName("Heavy arithmetic loop")
    void testHeavyLoop() {
        long sum = 0;
        for (int i = 0; i < 5_000_000; i++) {
            sum += new Calculator().add(i, 1);
        }
        assertTrue(sum > 0);
    }
}
