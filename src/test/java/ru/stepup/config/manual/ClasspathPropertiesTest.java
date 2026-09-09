package ru.stepup.config.manual;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Properties;
import org.junit.jupiter.api.Test;

/**
 * =====================================================================
 *  ClasspathPropertiesTest — проверка чтения свойств через getResourceAsStream
 * =====================================================================
 *  Материал вебинара «Конфигурирование проекта», способ №1.
 *  Тест доказывает, что файл из src/main/resources реально находится
 *  в classpath и корректно читается.
 */
class ClasspathPropertiesTest {

    /** Ресурс лежит в src/main/resources/config/manual-example.properties. */
    private static final String RESOURCE = "config/manual-example.properties";

    @Test
    void loadsPropertiesFromClasspathResource() {
        // when: читаем properties-файл из classpath
        Properties props = ClasspathProperties.load(RESOURCE);

        // then: значения на месте и доступны «ручным» способом
        assertThat(props.getProperty("host")).isEqualTo("127.0.0.1");
        assertThat(props.getProperty("port")).isEqualTo("8080");
        assertThat(props.getProperty("username")).isEqualTo("admin");
    }

    @Test
    void helperReturnsDefaultWhenKeyIsMissing() {
        Properties props = ClasspathProperties.load(RESOURCE);

        // Ключа "timeout" в файле нет — вернётся дефолт, а не null/NPE.
        assertThat(ClasspathProperties.get(props, "missing", "fallback")).isEqualTo("fallback");
    }

    @Test
    void throwsMeaningfulErrorWhenResourceNotFound() {
        // getResourceAsStream при отсутствии ресурса возвращает null,
        // а наш класс превращает это в понятное исключение.
        assertThatThrownBy(() -> ClasspathProperties.load("config/no-such-file.properties"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Ресурс не найден");
    }
}
