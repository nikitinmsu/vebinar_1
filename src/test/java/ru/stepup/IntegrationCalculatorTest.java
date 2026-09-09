package ru.stepup;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Пример «интеграционного» теста.
 *
 * ВАЖНО: имя класса намеренно начинается с "Integration" — именно поэтому его
 * подхватывает задача integrationTest (её include-паттерн «все классы,
 * начинающиеся с Integration»). Сам юнит-тест task `test` этот класс исключает.
 *
 * В отличие от юнит-тестов, проверяющих ОДИН класс, здесь проверяется
 * совместная работа нескольких компонентов проекта: Calculator + StringUtils + User.
 */
@Tag("integration")
class IntegrationCalculatorTest {

    private final Calculator calculator = new Calculator();

    @Test
    @Tag("test_build_report")
    @DisplayName("Report is built correctly from several components")
    void testBuildReport() {
        // «Интеграция»: одновременно используем Calculator, StringUtils и User
        String report = buildReport(List.of("Alice", "Bob"));
        assertTrue(report.contains("Total: 20"));
        assertTrue(report.contains("employees:"));
    }

    @Test
    @DisplayName("User lifecycle across components")
    void testUserLifecycle() {
        User user = new User("Alice", "alice@example.com", 20);
        user.haveBirthday();

        assertTrue(user.isAdult());
        assertTrue(StringUtils.toSnakeCase(user.getName()).contains("alice"));
    }

    // Собираем «отчёт» из нескольких классов — типичный интеграционный сценарий
    private String buildReport(List<String> names) {
        StringBuilder employees = new StringBuilder();
        for (String name : names) {
            employees.append("Employee: ").append(name).append('\n');
        }
        int total = calculator.multiply(names.size(), 10);
        return "Total: " + total + "\n"
                + "employees: " + StringUtils.countVowels(employees.toString());
    }
}
